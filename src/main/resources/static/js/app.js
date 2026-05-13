const state = {
    page: 0,
    totalPages: 1,
    size: 12,
    currentUser: null,
    categories: [],
    brands: [],
    suppliers: [],
    warehouses: [],
    supplyProducts: []
};

const rub = new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', maximumFractionDigits: 0 });
const dateFmt = new Intl.DateTimeFormat('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric' });
const orderStatuses = {
    CREATED: 'Создан',
    PAID: 'Оплачен',
    PROCESSING: 'В обработке',
    SHIPPED: 'Отгружен',
    DELIVERED: 'Доставлен',
    CANCELLED: 'Отменен',
    CANCELED: 'Отменен'
};
const orderStatusOptions = ['CREATED', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
let showAllOrders = false;

function esc(value) {
    return String(value ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[char]));
}

function money(value) {
    return rub.format(Number(value || 0));
}

function shortDate(value) {
    return value ? dateFmt.format(new Date(value)) : '-';
}

function orderStatusText(status) {
    return orderStatuses[status] || status || '-';
}

function debounce(fn, delay = 350) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => fn(...args), delay);
    };
}

function pageParam() {
    return `page=${state.page}&size=${state.size}`;
}

function getPageMeta(page) {
    const meta = page.page || page;
    return {
        number: Number(meta.number ?? state.page ?? 0),
        size: Number(meta.size ?? state.size ?? 20),
        totalElements: Number(meta.totalElements ?? page.totalElements ?? 0),
        totalPages: Number(meta.totalPages ?? page.totalPages ?? 0)
    };
}

function getPageContent(page) {
    return Array.isArray(page.content) ? page.content : [];
}

function applyPageInfo(page, reloadFn) {
    const meta = getPageMeta(page);
    const totalPages = Math.max(meta.totalPages, 1);
    state.totalPages = totalPages;
    state.page = Math.max(meta.number, 0);
    state.size = Math.max(meta.size, 1);

    if (state.page > totalPages - 1) {
        state.page = totalPages - 1;
        reloadFn();
        return false;
    }

    if (state.page < 0) {
        state.page = 0;
        reloadFn();
        return false;
    }

    return true;
}

function renderAttributes(attributes, limit) {
    const list = Array.isArray(attributes) ? attributes.filter(attr => attr && attr.name) : [];
    const shown = typeof limit === 'number' ? list.slice(0, limit) : list;

    if (!shown.length) {
        return '';
    }

    return shown.map(attr => `<span class="attribute-pill">${esc(attr.name)}: ${esc(attr.value || '-')}</span>`).join('');
}

function getAvailableStocks(stocks) {
    return Array.isArray(stocks)
        ? stocks.filter(stock => Number(stock.quantity || 0) > 0)
        : [];
}

function getTotalStock(product) {
    return getAvailableStocks(product.stocks)
        .reduce((sum, stock) => sum + Number(stock.quantity || 0), 0);
}

function renderStockBadge(product) {
    const total = getTotalStock(product);
    return total > 0
        ? `<span class="badge ok">В наличии: ${total} шт.</span>`
        : '<span class="badge warn">Нет в наличии</span>';
}

function getCartQuantity(productId) {
    const item = getCart().find(cartItem => Number(cartItem.id) === Number(productId));
    return Number(item?.quantity || 0);
}

function renderCartControl(product) {
    const stock = getTotalStock(product);
    const quantity = getCartQuantity(product.id);

    if (stock <= 0) {
        return '<button class="btn" disabled>Нет в наличии</button>';
    }

    if (quantity <= 0) {
        return `<button class="btn" data-add="${product.id}">В корзину</button>`;
    }

    return `
        <div class="qty-stepper" data-stepper="${product.id}">
            <button class="btn secondary" data-dec="${product.id}" type="button">-</button>
            <span>${quantity}</span>
            <button class="btn secondary" data-inc="${product.id}" type="button" ${quantity >= stock ? 'disabled' : ''}>+</button>
        </div>
    `;
}

function deliveryDaysText(days) {
    const value = Number(days || 0);
    if (value === 1) return '1 день';
    if (value > 1 && value < 5) return `${value} дня`;
    return `${value} дней`;
}

function renderDeliveryOptions(stocks) {
    const available = getAvailableStocks(stocks);

    if (!available.length) {
        return '<div class="notice">Сейчас товара нет на складах.</div>';
    }

    return `
        <div class="stock-list">
            ${available.map(stock => `
                <div class="stock-row">
                    <div>
                        <strong>${esc(stock.warehouseAddress || `Склад #${stock.warehouseId}`)}</strong>
                        <div class="meta">Доступно: ${Number(stock.quantity || 0)} шт.</div>
                    </div>
                    <span class="badge">Доставка ${deliveryDaysText(stock.warehouseDeliveryDays)}</span>
                </div>
            `).join('')}
        </div>
    `;
}

function setActiveNav() {
    const file = location.pathname.split('/').pop() || 'index.html';
    document.querySelectorAll('.nav a').forEach(link => {
        link.classList.toggle('active', link.getAttribute('href') === file);
    });
}

async function initShell(adminOnly = false) {
    requireAuth();
    updateCartCount();
    setActiveNav();

    try {
        state.currentUser = await getCurrentUser();
        document.querySelectorAll('[data-admin-only]').forEach(el => {
            el.hidden = state.currentUser.role !== 'ADMIN';
        });

        if (adminOnly && state.currentUser.role !== 'ADMIN') {
            location.href = 'index.html';
        }
    } catch (err) {
        logout();
    }
}

function renderPagination(containerId, loadFn) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const totalPages = Math.max(state.totalPages, 1);
    const currentPage = Math.min(Math.max(state.page, 0), totalPages - 1);

    container.innerHTML = `
        <button class="btn secondary" ${currentPage <= 0 ? 'disabled' : ''} data-prev>Назад</button>
        <span class="meta">Страница ${currentPage + 1} из ${totalPages}</span>
        <button class="btn secondary" ${currentPage >= totalPages - 1 ? 'disabled' : ''} data-next>Вперед</button>
    `;

    container.querySelector('[data-prev]')?.addEventListener('click', () => {
        state.page = Math.max(0, currentPage - 1);
        loadFn();
    });
    container.querySelector('[data-next]')?.addEventListener('click', () => {
        state.page = Math.min(totalPages - 1, currentPage + 1);
        loadFn();
    });
}

function openModal(id) {
    document.getElementById(id).classList.add('open');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

async function loadDictionaries() {
    const [categories, brands] = await Promise.all([
        apiGet('/categories').catch(() => []),
        apiGet('/brands').catch(() => [])
    ]);
    state.categories = categories;
    state.brands = brands;
}

function fillSelect(id, items, label, selected) {
    const el = document.getElementById(id);
    if (!el) return;

    el.innerHTML = `<option value="">${label}</option>` + items.map(item => {
        const name = item.name || item.address || `ID ${item.id}`;
        return `<option value="${item.id}" ${Number(selected) === Number(item.id) ? 'selected' : ''}>${esc(name)}</option>`;
    }).join('');
}

async function initLogin() {
    if (isLoggedIn()) {
        location.href = 'index.html';
        return;
    }

    document.getElementById('loginForm').addEventListener('submit', async event => {
        event.preventDefault();
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const error = document.getElementById('loginError');

        setCredentials(username, password);
        error.textContent = '';

        try {
            await apiGet('/users/me');
            location.href = 'index.html';
        } catch (err) {
            localStorage.removeItem(AUTH_KEY);
            error.textContent = 'Неверный логин или пароль';
        }
    });
}

async function initCatalog() {
    await initShell();
    await loadDictionaries();
    fillSelect('filterCategory', state.categories, 'Все категории');
    fillSelect('filterBrand', state.brands, 'Все бренды');

    document.getElementById('filterForm').addEventListener('submit', event => {
        event.preventDefault();
        state.page = 0;
        loadCatalog();
    });
    document.getElementById('resetFilters').addEventListener('click', () => {
        document.getElementById('filterForm').reset();
        state.page = 0;
        loadCatalog();
    });
    document.getElementById('catalogSearch').addEventListener('input', debounce(() => {
        state.page = 0;
        loadCatalog();
    }));

    await loadCatalog();
}

function getProductFilter() {
    const form = document.getElementById('filterForm');
    const data = new FormData(form);
    return {
        query: data.get('query') || null,
        name: data.get('name') || null,
        categoryId: data.get('categoryId') ? Number(data.get('categoryId')) : null,
        brandId: data.get('brandId') ? Number(data.get('brandId')) : null,
        sku: data.get('sku') || null,
        oemNumber: data.get('oemNumber') || null,
        minPrice: data.get('minPrice') ? Number(data.get('minPrice')) : null,
        maxPrice: data.get('maxPrice') ? Number(data.get('maxPrice')) : null,
        inStock: data.get('inStock') === 'on' ? true : null,
        attributes: null
    };
}

async function loadCatalog() {
    const grid = document.getElementById('productGrid');
    grid.innerHTML = '<div class="notice">Загрузка каталога...</div>';

    try {
        const page = await apiPost(`/products/search?${pageParam()}&sortBy=id&sortDirection=asc`, getProductFilter());
        if (!applyPageInfo(page, loadCatalog)) return;
        const products = getPageContent(page);

        displayCatalogProducts(products);
        renderPagination('catalogPagination', loadCatalog);
    } catch (err) {
        grid.innerHTML = `<div class="notice">${esc(err.message)}</div>`;
    }
}

function displayCatalogProducts(products) {
    const grid = document.getElementById('productGrid');
    grid.innerHTML = products.length ? products.map(product => `
        <article class="product-card">
            <div class="product-card__media"></div>
            <div class="product-card__body">
                <span class="badge">${esc(product.category?.name || 'Запчасть')}</span>
                <h3>${esc(product.name)}</h3>
                <div class="meta">${esc(product.brand?.name || '-')} · SKU ${esc(product.sku || '-')} · OEM ${esc(product.oemNumber || '-')}</div>
                ${renderStockBadge(product)}
                <div class="attribute-list">${renderAttributes(product.attributes, 3)}</div>
                <div class="price">${money(product.sellingPrice)}</div>
                <div class="actions">
                    ${renderCartControl(product)}
                    <button class="btn secondary" data-view="${product.id}">Подробнее</button>
                </div>
            </div>
        </article>
    `).join('') : '<div class="empty">По выбранным фильтрам товары не найдены.</div>';

    grid.querySelectorAll('[data-add]').forEach(btn => {
        btn.addEventListener('click', () => {
            const product = products.find(item => Number(item.id) === Number(btn.dataset.add));
            if (addToCart(product)) {
                displayCatalogProducts(products);
            }
        });
    });
    grid.querySelectorAll('[data-inc]').forEach(btn => {
        btn.addEventListener('click', () => {
            const product = products.find(item => Number(item.id) === Number(btn.dataset.inc));
            addToCart(product);
            displayCatalogProducts(products);
        });
    });
    grid.querySelectorAll('[data-dec]').forEach(btn => {
        btn.addEventListener('click', () => {
            const product = products.find(item => Number(item.id) === Number(btn.dataset.dec));
            updateCartItem(product.id, Math.max(getCartQuantity(product.id) - 1, 0));
            displayCatalogProducts(products);
        });
    });
    grid.querySelectorAll('[data-view]').forEach(btn => {
        btn.addEventListener('click', () => showProduct(Number(btn.dataset.view)));
    });
}

async function showProduct(id) {
    const product = await apiGet(`/products/${id}`);
    const attributesHtml = renderAttributes(product.attributes);
    const totalStock = getTotalStock(product);
    document.getElementById('productModalBody').innerHTML = `
        <div class="product-card__media" style="height:150px;border-radius:8px;margin-bottom:16px"></div>
        <h2>${esc(product.name)}</h2>
        <p class="price">${money(product.sellingPrice)}</p>
        ${renderStockBadge(product)}
        <p>${esc(product.description || 'Описание не заполнено.')}</p>
        <p class="meta">Бренд: ${esc(product.brand?.name || '-')} · Категория: ${esc(product.category?.name || '-')}</p>
        <p class="meta">SKU: ${esc(product.sku || '-')} · OEM: ${esc(product.oemNumber || '-')}</p>
        <h3>Характеристики</h3>
        <div class="attribute-list detail">${attributesHtml || '<span class="meta">Атрибуты не заполнены.</span>'}</div>
        <h3>Наличие и доставка</h3>
        ${renderDeliveryOptions(product.stocks)}
        <div class="actions"><button class="btn" id="modalAddToCart" ${totalStock <= 0 ? 'disabled' : ''}>Добавить в корзину</button></div>
    `;
    document.getElementById('modalAddToCart').addEventListener('click', () => {
        addToCart(product);
        closeModal('productModal');
    });
    openModal('productModal');
}

async function initProductsAdmin() {
    await initShell(true);
    await loadDictionaries();
    fillSelect('productCategoryId', state.categories, 'Категория');
    fillSelect('productBrandId', state.brands, 'Бренд');
    await loadProductsAdmin();

    document.getElementById('productForm').addEventListener('submit', saveProduct);
    document.getElementById('createProduct').addEventListener('click', () => editProduct());
}

async function loadProductsAdmin() {
    const page = await apiPost(`/products/search?${pageParam()}&sortBy=id&sortDirection=asc`, {});
    if (!applyPageInfo(page, loadProductsAdmin)) return;
    const rows = getPageContent(page).map(product => `
        <tr>
            <td>${product.id}</td>
            <td><strong>${esc(product.name)}</strong><div class="meta">${esc(product.description || '')}</div></td>
            <td>${money(product.sellingPrice)}</td>
            <td>${esc(product.category?.name || '-')}</td>
            <td>${esc(product.brand?.name || '-')}</td>
            <td>${esc(product.sku || '-')}</td>
            <td class="actions">
                <button class="btn secondary" data-edit="${product.id}">Изменить</button>
                <button class="btn danger" data-delete="${product.id}">Удалить</button>
            </td>
        </tr>
    `).join('');

    document.querySelector('#productsTable tbody').innerHTML = rows || '<tr><td colspan="7">Товаров пока нет.</td></tr>';
    document.querySelectorAll('[data-edit]').forEach(btn => btn.addEventListener('click', () => editProduct(btn.dataset.edit)));
    document.querySelectorAll('[data-delete]').forEach(btn => btn.addEventListener('click', () => deleteProduct(btn.dataset.delete)));
    renderPagination('productsPagination', loadProductsAdmin);
}

async function editProduct(id) {
    const form = document.getElementById('productForm');
    form.reset();
    document.getElementById('productId').value = '';
    document.getElementById('productModalTitle').textContent = id ? 'Редактирование товара' : 'Новый товар';

    if (id) {
        const product = await apiGet(`/products/${id}`);
        document.getElementById('productId').value = product.id;
        form.name.value = product.name || '';
        form.sellingPrice.value = product.sellingPrice || '';
        form.categoryId.value = product.category?.id || '';
        form.brandId.value = product.brand?.id || '';
        form.sku.value = product.sku || '';
        form.oemNumber.value = product.oemNumber || '';
        form.description.value = product.description || '';
    }

    openModal('productEditModal');
}

async function saveProduct(event) {
    event.preventDefault();
    const form = event.target;
    const id = document.getElementById('productId').value;
    const payload = {
        name: form.name.value.trim(),
        sellingPrice: Number(form.sellingPrice.value),
        categoryId: Number(form.categoryId.value),
        brandId: Number(form.brandId.value),
        sku: form.sku.value.trim(),
        oemNumber: form.oemNumber.value.trim(),
        description: form.description.value.trim(),
        attributes: []
    };

    if (id) await apiPut(`/products/${id}`, payload);
    else await apiPost('/products', payload);

    closeModal('productEditModal');
    loadProductsAdmin();
}

async function deleteProduct(id) {
    if (!confirm('Удалить товар?')) return;
    await apiDelete(`/products/${id}`);
    loadProductsAdmin();
}

async function initCart() {
    await initShell();
    await refreshCartAvailability();
    renderCart();
    document.getElementById('checkoutForm').addEventListener('submit', submitOrder);
    document.getElementById('openCheckout').addEventListener('click', () => {
        document.getElementById('deliveryAddress').value = state.currentUser?.deliveryAddress || '';
        openModal('checkoutModal');
    });
}

async function refreshCartAvailability() {
    const cart = getCart();
    if (!cart.length) return;

    const refreshed = await Promise.all(cart.map(async item => {
        try {
            const product = await apiGet(`/products/${item.id}`);
            const availableQuantity = getTotalStock(product);
            return {
                ...item,
                name: product.name,
                sku: product.sku || item.sku || '',
                brand: product.brand?.name || item.brand || '',
                price: Number(product.sellingPrice || item.price || 0),
                availableQuantity,
                quantity: Math.min(Number(item.quantity || 1), availableQuantity)
            };
        } catch (err) {
            return item;
        }
    }));

    saveCart(refreshed.filter(item => Number(item.quantity || 0) > 0));
}

function renderCart() {
    const cart = getCart();
    const box = document.getElementById('cartList');
    const total = document.getElementById('cartTotal');
    const checkout = document.getElementById('openCheckout');

    box.innerHTML = cart.length ? cart.map(item => `
        <div class="cart-item">
            <div><strong>${esc(item.name)}</strong><div class="meta">SKU ${esc(item.sku || '-')} ${item.brand ? '· ' + esc(item.brand) : ''} · В наличии ${Number(item.availableQuantity || 0)} шт.</div></div>
            <input class="cart-qty" type="number" min="1" max="${Number(item.availableQuantity || item.quantity)}" value="${item.quantity}" data-qty="${item.id}">
            <strong>${money(item.price * item.quantity)}</strong>
            <button class="btn danger" data-remove="${item.id}">Удалить</button>
        </div>
    `).join('') : '<div class="empty">Корзина пустая. Добавьте товары из каталога.</div>';

    total.textContent = money(getCartTotal());
    checkout.disabled = cart.length === 0;

    box.querySelectorAll('[data-qty]').forEach(input => input.addEventListener('input', () => {
        const max = Number(input.max || input.value);
        if (Number(input.value) > max) {
            input.value = max;
        }
        if (Number(input.value) < 1) {
            input.value = 1;
        }
        updateCartItem(input.dataset.qty, input.value);
        renderCart();
    }));
    box.querySelectorAll('[data-qty]').forEach(input => input.addEventListener('change', () => {
        const max = Number(input.max || input.value);
        if (Number(input.value) > max) {
            input.value = max;
        }
        if (Number(input.value) < 1) {
            input.value = 1;
        }
        updateCartItem(input.dataset.qty, input.value);
        renderCart();
    }));
    box.querySelectorAll('[data-remove]').forEach(btn => btn.addEventListener('click', () => {
        removeFromCart(btn.dataset.remove);
        renderCart();
    }));
}

async function submitOrder(event) {
    event.preventDefault();
    await refreshCartAvailability();
    const cart = getCart();
    const invalidItem = cart.find(item => Number(item.quantity) > Number(item.availableQuantity || item.quantity));
    if (invalidItem) {
        alert(`Товара "${invalidItem.name}" в наличии только ${invalidItem.availableQuantity} шт.`);
        renderCart();
        return;
    }

    const payload = {
        userId: state.currentUser.id,
        deliveryAddress: document.getElementById('deliveryAddress').value.trim(),
        items: cart.map(item => ({ productId: item.id, quantity: item.quantity }))
    };

    await apiPost('/orders', payload);
    clearCart();
    location.href = 'orders.html';
}

async function initOrders() {
    await initShell();
    state.size = 20;
    const toggle = document.getElementById('toggleAllOrders');
    if (toggle) {
        toggle.addEventListener('click', () => {
            showAllOrders = !showAllOrders;
            state.page = 0;
            loadOrders();
        });
    }
    await loadOrders();
}

async function loadOrders() {
    const page = await apiGet(`/orders?${pageParam()}`);
    if (!applyPageInfo(page, loadOrders)) return;
    const adminViewingAll = state.currentUser.role === 'ADMIN' && showAllOrders;
    const orders = getPageContent(page).filter(order => adminViewingAll || order.userId === state.currentUser.id);
    const toggle = document.getElementById('toggleAllOrders');
    const hint = document.getElementById('ordersPageHint');

    if (toggle) {
        toggle.textContent = showAllOrders ? 'Мои заказы' : 'Заказы других пользователей';
    }
    if (hint) {
        hint.textContent = adminViewingAll ? 'Все заказы клиентов с управлением статусами.' : 'История оформленных заказов.';
    }

    document.querySelector('#ordersTable tbody').innerHTML = orders.map(order => `
        <tr>
            <td>#${order.id}</td>
            <td>
                ${adminViewingAll ? `
                    <select class="status-select" data-status-order="${order.id}">
                        ${orderStatusOptions.map(status => `<option value="${status}" ${status === order.orderStatus ? 'selected' : ''}>${esc(orderStatusText(status))}</option>`).join('')}
                    </select>
                    <button class="btn secondary" data-save-status="${order.id}">Сохранить</button>
                ` : `<span class="badge ${order.orderStatus === 'CREATED' ? 'warn' : 'ok'}">${esc(orderStatusText(order.orderStatus))}</span>`}
            </td>
            <td>${money(order.totalPrice)}</td>
            <td>${shortDate(order.dateOfPurchase)}</td>
            <td>${shortDate(order.dateOfDelivery)}</td>
            <td>${esc(order.deliveryAddress || '-')}${adminViewingAll ? `<div class="meta">Пользователь ID: ${esc(order.userId)}</div>` : ''}</td>
        </tr>
    `).join('') || '<tr><td colspan="6">Заказов пока нет.</td></tr>';

    document.querySelectorAll('[data-save-status]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const select = document.querySelector(`[data-status-order="${btn.dataset.saveStatus}"]`);
            await apiPut(`/orders/${btn.dataset.saveStatus}/status?status=${encodeURIComponent(select.value)}`, null);
            loadOrders();
        });
    });
    renderPagination('ordersPagination', loadOrders);
}

async function initSupplies() {
    await initShell(true);
    state.size = 20;
    const [suppliers, warehouses, productsPage] = await Promise.all([
        apiGet('/suppliers'),
        apiGet('/warehouses'),
        apiGet('/products?page=0&size=200&sortBy=id&sortDirection=asc')
    ]);
    state.suppliers = suppliers;
    state.warehouses = warehouses;
    state.supplyProducts = getPageContent(productsPage);
    fillSelect('supplierId', state.suppliers, 'Поставщик');
    document.getElementById('createSupply').addEventListener('click', openSupplyForm);
    document.getElementById('addSupplyItem').addEventListener('click', addSupplyItem);
    document.getElementById('supplyForm').addEventListener('submit', saveSupply);
    await loadSupplies();
}

async function loadSupplies() {
    const page = await apiGet(`/supplies?${pageParam()}&sortBy=id&sortDirection=desc`);
    if (!applyPageInfo(page, loadSupplies)) return;
    document.querySelector('#suppliesTable tbody').innerHTML = getPageContent(page).map(supply => `
        <tr>
            <td>#${supply.id}</td>
            <td>${esc(state.suppliers.find(item => item.id === supply.supplierId)?.name || supply.supplierId)}</td>
            <td>${shortDate(supply.dateOfSupply)}</td>
            <td>${(supply.items || []).length}</td>
            <td><button class="btn danger" data-delete-supply="${supply.id}">Удалить</button></td>
        </tr>
    `).join('') || '<tr><td colspan="5">Поставок пока нет.</td></tr>';
    document.querySelectorAll('[data-delete-supply]').forEach(btn => btn.addEventListener('click', async () => {
        if (confirm('Удалить поставку?')) {
            await apiDelete(`/supplies/${btn.dataset.deleteSupply}`);
            loadSupplies();
        }
    }));
    renderPagination('suppliesPagination', loadSupplies);
}

function openSupplyForm() {
    document.getElementById('supplyForm').reset();
    document.getElementById('supplyItems').innerHTML = '';
    addSupplyItem();
    openModal('supplyModal');
}

function addSupplyItem() {
    const row = document.createElement('div');
    row.className = 'supply-item';
    row.innerHTML = `
        <div class="field"><label>Товар</label><select name="productId" required>${state.supplyProducts.map(product => `<option value="${product.id}">#${product.id} ${esc(product.name)} (${esc(product.sku || 'без SKU')})</option>`).join('')}</select></div>
        <div class="field"><label>Склад</label><select name="warehouseId" required>${state.warehouses.map(w => `<option value="${w.id}">${esc(w.address)}</option>`).join('')}</select></div>
        <div class="field"><label>Кол-во</label><input name="quantity" type="number" min="1" required></div>
        <div class="field"><label>Цена закупки</label><input name="purchasePrice" type="number" min="0" step="0.01" required></div>
        <button class="btn danger" type="button">Удалить</button>
    `;
    row.querySelector('button').addEventListener('click', () => row.remove());
    document.getElementById('supplyItems').appendChild(row);
}

async function saveSupply(event) {
    event.preventDefault();
    const items = [...document.querySelectorAll('.supply-item')].map(row => ({
        productId: Number(row.querySelector('[name="productId"]').value),
        warehouseId: Number(row.querySelector('[name="warehouseId"]').value),
        quantity: Number(row.querySelector('[name="quantity"]').value),
        purchasePrice: Number(row.querySelector('[name="purchasePrice"]').value)
    }));
    const payload = {
        supplierId: Number(document.getElementById('supplierId').value),
        dateOfSupply: new Date(document.getElementById('dateOfSupply').value || Date.now()).toISOString(),
        items
    };
    await apiPost('/supplies', payload);
    closeModal('supplyModal');
    loadSupplies();
}

async function initWarehouses() {
    await initShell(true);
    document.getElementById('createWarehouse').addEventListener('click', () => editWarehouse());
    document.getElementById('warehouseForm').addEventListener('submit', saveWarehouse);
    await loadWarehouses();
}

async function loadWarehouses() {
    state.warehouses = await apiGet('/warehouses');
    document.querySelector('#warehousesTable tbody').innerHTML = state.warehouses.map(warehouse => `
        <tr>
            <td>#${warehouse.id}</td>
            <td>${esc(warehouse.address)}</td>
            <td>${esc(warehouse.phone)}</td>
            <td>${deliveryDaysText(warehouse.deliveryDays)}</td>
            <td class="actions">
                <button class="btn secondary" data-edit-warehouse="${warehouse.id}">Изменить</button>
                <button class="btn danger" data-delete-warehouse="${warehouse.id}">Удалить</button>
            </td>
        </tr>
    `).join('') || '<tr><td colspan="5">Складов пока нет.</td></tr>';

    document.querySelectorAll('[data-edit-warehouse]').forEach(btn => btn.addEventListener('click', () => editWarehouse(btn.dataset.editWarehouse)));
    document.querySelectorAll('[data-delete-warehouse]').forEach(btn => btn.addEventListener('click', () => deleteWarehouse(btn.dataset.deleteWarehouse)));
}

async function editWarehouse(id) {
    const form = document.getElementById('warehouseForm');
    form.reset();
    document.getElementById('warehouseId').value = '';
    document.getElementById('warehouseModalTitle').textContent = id ? 'Редактирование склада' : 'Новый склад';

    if (id) {
        const warehouse = await apiGet(`/warehouses/${id}`);
        document.getElementById('warehouseId').value = warehouse.id;
        form.address.value = warehouse.address || '';
        form.phone.value = warehouse.phone || '';
        form.deliveryDays.value = warehouse.deliveryDays ?? '';
    }

    openModal('warehouseModal');
}

async function saveWarehouse(event) {
    event.preventDefault();
    const form = event.target;
    const id = document.getElementById('warehouseId').value;
    const payload = {
        address: form.address.value.trim(),
        phone: form.phone.value.trim(),
        deliveryDays: Number(form.deliveryDays.value)
    };

    if (id) await apiPut(`/warehouses/${id}`, payload);
    else await apiPost('/warehouses', payload);

    closeModal('warehouseModal');
    loadWarehouses();
}

async function deleteWarehouse(id) {
    if (!confirm('Удалить склад?')) return;
    await apiDelete(`/warehouses/${id}`);
    loadWarehouses();
}

async function initUsers() {
    await initShell(true);
    state.size = 20;
    document.getElementById('createUser').addEventListener('click', () => editUser());
    document.getElementById('userForm').addEventListener('submit', saveUser);
    await loadUsers();
}

async function loadUsers() {
    const page = await apiGet(`/users?${pageParam()}`);
    if (!applyPageInfo(page, loadUsers)) return;
    document.querySelector('#usersTable tbody').innerHTML = getPageContent(page).map(user => `
        <tr>
            <td><strong>${esc(user.username)}</strong><div class="meta">ID ${user.id}</div></td>
            <td>${esc(user.email)}</td>
            <td>${esc(user.phone || '-')}</td>
            <td><span class="badge ${user.role === 'ADMIN' ? 'warn' : ''}">${esc(user.role)}</span></td>
            <td class="actions">
                <button class="btn secondary" data-edit-user="${user.id}">Изменить</button>
                <button class="btn danger" data-delete-user="${user.id}">Удалить</button>
            </td>
        </tr>
    `).join('') || '<tr><td colspan="5">Пользователей пока нет.</td></tr>';
    document.querySelectorAll('[data-edit-user]').forEach(btn => btn.addEventListener('click', () => editUser(btn.dataset.editUser)));
    document.querySelectorAll('[data-delete-user]').forEach(btn => btn.addEventListener('click', () => deleteUser(btn.dataset.deleteUser)));
    renderPagination('usersPagination', loadUsers);
}

async function editUser(id) {
    const form = document.getElementById('userForm');
    form.reset();
    document.getElementById('userId').value = '';
    form.password.required = !id;
    document.getElementById('userModalTitle').textContent = id ? 'Редактирование пользователя' : 'Новый пользователь';

    if (id) {
        const user = await apiGet(`/users/${id}`);
        document.getElementById('userId').value = user.id;
        form.username.value = user.username || '';
        form.email.value = user.email || '';
        form.deliveryAddress.value = user.deliveryAddress || '';
        form.companyName.value = user.companyName || '';
        form.phone.value = user.phone || '';
        form.role.value = user.role || 'USER';
        form.priceLevelId.value = user.priceLevelId || '';
    }

    openModal('userModal');
}

async function saveUser(event) {
    event.preventDefault();
    const form = event.target;
    const id = document.getElementById('userId').value;
    const payload = {
        username: form.username.value.trim(),
        password: form.password.value || null,
        email: form.email.value.trim(),
        deliveryAddress: form.deliveryAddress.value.trim(),
        companyName: form.companyName.value.trim(),
        phone: form.phone.value.trim(),
        role: form.role.value,
        priceLevelId: form.priceLevelId.value ? Number(form.priceLevelId.value) : null
    };
    if (id) await apiPut(`/users/${id}`, payload);
    else await apiPost('/users', payload);
    closeModal('userModal');
    loadUsers();
}

async function deleteUser(id) {
    if (!confirm('Удалить пользователя?')) return;
    await apiDelete(`/users/${id}`);
    loadUsers();
}

document.addEventListener('click', event => {
    if (event.target.classList.contains('modal')) {
        event.target.classList.remove('open');
        return;
    }

    const closeTarget = event.target.closest('[data-close-modal]');
    if (closeTarget) closeModal(closeTarget.dataset.closeModal);
});

document.addEventListener('keydown', event => {
    if (event.key === 'Escape') {
        document.querySelectorAll('.modal.open').forEach(modal => modal.classList.remove('open'));
    }
});

window.addEventListener('load', () => {
    const page = document.body.dataset.page;
    const init = {
        login: initLogin,
        catalog: initCatalog,
        products: initProductsAdmin,
        cart: initCart,
        orders: initOrders,
        supplies: initSupplies,
        warehouses: initWarehouses,
        users: initUsers
    }[page];

    if (init) {
        init().catch(err => {
            console.error(err);
            alert(err.message || 'Ошибка выполнения операции');
        });
    }
});
