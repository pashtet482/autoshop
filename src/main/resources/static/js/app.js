const state = {
    page: 0,
    totalPages: 1,
    size: 20,
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
const TAX_PERCENT = 22;
const PRODUCT_IMAGES_KEY = 'autoshop.productImages';
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

function percent(value) {
    return `${Number(value || 0).toLocaleString('ru-RU', { maximumFractionDigits: 2 })}%`;
}

function getProductImages() {
    return JSON.parse(localStorage.getItem(PRODUCT_IMAGES_KEY) || '{}');
}

function getProductImage(product) {
    return product.imageUrl || '';
}

function saveProductImage(productId, imageUrl) {
    const images = getProductImages();
    if (imageUrl) {
        images[productId] = imageUrl;
    } else {
        delete images[productId];
    }
    localStorage.setItem(PRODUCT_IMAGES_KEY, JSON.stringify(images));
}

function productMediaHtml(product, extraStyle = '') {
    const imageUrl = getProductImage(product);
    return `
        <div class="product-card__media" ${extraStyle ? `style="${extraStyle}"` : ''}>
            ${imageUrl ? `<img src="${esc(imageUrl)}" alt="${esc(product.name)}">` : ''}
        </div>
    `;
}

function productThumbHtml(product) {
    const imageUrl = getProductImage(product);
    return `<div class="product-thumb">${imageUrl ? `<img src="${esc(imageUrl)}" alt="${esc(product.name)}">` : ''}</div>`;
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

function getPriceRatio() {
    return Number(state.currentUser?.priceLevelRatio || 1);
}

function getDiscountPercent(ratio = getPriceRatio()) {
    return Math.max(0, (1 - ratio) * 100);
}

function getCartTotals() {
    const subtotal = getCartTotal();
    const ratio = getPriceRatio();
    const discountedSubtotal = subtotal * ratio;
    const discountAmount = subtotal - discountedSubtotal;
    const taxAmount = discountedSubtotal * TAX_PERCENT / 100;

    return {
        subtotal,
        ratio,
        discountAmount,
        discountPercent: getDiscountPercent(ratio),
        taxAmount,
        total: discountedSubtotal + taxAmount
    };
}

function renderCheckoutSummary() {
    const box = document.getElementById('checkoutSummary');
    if (!box) return;

    const totals = getCartTotals();
    box.innerHTML = `
        <div class="checkout-summary__row"><span>Сумма товаров</span><strong>${money(totals.subtotal)}</strong></div>
        <div class="checkout-summary__row"><span>Скидка по уровню цены (${percent(totals.discountPercent)})</span><strong>-${money(totals.discountAmount)}</strong></div>
        <div class="checkout-summary__row"><span>Цена без налога</span><strong>${money(totals.subtotal - totals.discountAmount)}</strong></div>
        <div class="checkout-summary__row"><span>Налог (${TAX_PERCENT}%)</span><strong>${money(totals.taxAmount)}</strong></div>
        <div class="checkout-summary__row"><span>Итого к оплате</span><strong>${money(totals.total)}</strong></div>
    `;
}

function splitDeliveryAddress(address = '') {
    const value = String(address || '');
    const labeledParts = Object.fromEntries(
        value
            .split(';')
            .map(part => part.trim())
            .filter(Boolean)
            .map(part => {
                const separatorIndex = part.indexOf(':');
                if (separatorIndex < 0) return ['', ''];
                return [
                    part.slice(0, separatorIndex).trim().toLowerCase(),
                    part.slice(separatorIndex + 1).trim()
                ];
            })
            .filter(([key]) => key)
    );

    if (Object.keys(labeledParts).length) {
        return {
            city: labeledParts['город'] || '',
            street: labeledParts['улица/дом'] || '',
            apartment: labeledParts['кв./офис'] || '',
            postalCode: labeledParts['индекс'] || ''
        };
    }

    const parts = value.split(',').map(part => part.trim());
    return {
        postalCode: parts.find(part => /^\d{6}$/.test(part)) || '',
        city: parts.find(part => part && !/^\d{6}$/.test(part)) || '',
        street: parts.length > 2 ? parts.slice(2, -1).join(', ') : '',
        apartment: parts.length > 3 ? parts[parts.length - 1] : ''
    };
}

function deliveryField(root, name) {
    return root.querySelector?.(`[name="${name}"]`) || document.getElementById(name);
}

function fillDeliveryAddressFields(address, root = document) {
    const parsed = splitDeliveryAddress(address);
    const city = deliveryField(root, 'deliveryCity');
    const postalCode = deliveryField(root, 'deliveryPostalCode');
    const street = deliveryField(root, 'deliveryStreet');
    const apartment = deliveryField(root, 'deliveryApartment');

    if (city) city.value = parsed.city;
    if (postalCode) postalCode.value = parsed.postalCode;
    if (street) street.value = parsed.street;
    if (apartment) apartment.value = parsed.apartment;
}

function collectDeliveryAddress(root = document) {
    const city = deliveryField(root, 'deliveryCity')?.value.trim();
    const postalCode = deliveryField(root, 'deliveryPostalCode')?.value.trim();
    const street = deliveryField(root, 'deliveryStreet')?.value.trim();
    const apartment = deliveryField(root, 'deliveryApartment')?.value.trim();

    return [
        city ? `Город: ${city}` : '',
        street ? `Улица/дом: ${street}` : '',
        apartment ? `Кв./офис: ${apartment}` : '',
        postalCode ? `Индекс: ${postalCode}` : ''
    ].filter(Boolean).join('; ');
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
    if (!form) return {};
    const fd = new FormData(form);
    const query = (fd.get('query') || '').toString().trim() || null;
    const categoryId = fd.get('categoryId') ? Number(fd.get('categoryId')) : null;
    const brandId = fd.get('brandId') ? Number(fd.get('brandId')) : null;
    const minPrice = fd.get('minPrice') ? Number(fd.get('minPrice')) : null;
    const maxPrice = fd.get('maxPrice') ? Number(fd.get('maxPrice')) : null;
    const inStock = form.inStock && form.inStock.checked ? true : null;

    return {
        query: query,
        name: null,
        minPrice: minPrice,
        maxPrice: maxPrice,
        categoryId: categoryId,
        brandId: brandId,
        sku: null,
        oemNumber: null,
        inStock: inStock,
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
            ${productMediaHtml(product)}
            <div class="product-card__body">
                <div class="product-card__main">
                    <span class="badge">${esc(product.category?.name || 'Запчасть')}</span>
                    <h3 title="${esc(product.name)}">${esc(product.name)}</h3>
                    <div class="meta product-card__meta">${esc(product.brand?.name || '-')} · SKU ${esc(product.sku || '-')} · OEM ${esc(product.oemNumber || '-')}</div>
                    ${renderStockBadge(product)}
                    <div class="attribute-list">${renderAttributes(product.attributes, 3)}</div>
                </div>
                <div class="product-card__footer">
                    <div class="price">${money(product.sellingPrice)}</div>
                    <div class="actions product-card__actions">
                        ${renderCartControl(product)}
                        <button class="btn secondary" data-view="${product.id}">Подробнее</button>
                    </div>
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
        <div class="product-modal">
            <div class="product-modal__media">
                ${productMediaHtml(product)}
            </div>
            <div class="product-modal__info">
                <div class="product-modal__header">
                    <div class="product-modal__title">
                        <span class="badge">${esc(product.category?.name || 'Запчасть')}</span>
                        <h2>${esc(product.name)}</h2>
                    </div>
                    <div class="product-modal__buy">
                        <p class="price">${money(product.sellingPrice)}</p>
                        ${renderStockBadge(product)}
                    </div>
                </div>

                <div class="product-modal__section">
                    <p class="product-modal__description">${esc(product.description || 'Описание не заполнено.')}</p>
                </div>

                <div class="product-modal__section product-modal__meta">
                    <div class="product-modal__meta-row"><span class="product-modal__meta-label">Бренд</span><span class="product-modal__meta-value">${esc(product.brand?.name || '-')}</span></div>
                    <div class="product-modal__meta-row"><span class="product-modal__meta-label">SKU</span><span class="product-modal__meta-value">${esc(product.sku || '-')}</span></div>
                    <div class="product-modal__meta-row"><span class="product-modal__meta-label">OEM</span><span class="product-modal__meta-value">${esc(product.oemNumber || '-')}</span></div>
                </div>

                <div class="product-modal__section">
                    <h3>Характеристики</h3>
                    <div class="attribute-list detail">${attributesHtml || '<span class="meta">Атрибуты не заполнены.</span>'}</div>
                </div>

                <div class="product-modal__section">
                    <h3>Наличие и доставка</h3>
                    ${renderDeliveryOptions(product.stocks)}
                </div>

                <div class="actions product-modal__actions">
                    <button class="btn" id="modalAddToCart" ${totalStock <= 0 ? 'disabled' : ''}>Добавить в корзину</button>
                </div>
            </div>
        </div>
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
            <td>${productThumbHtml(product)}</td>
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

    document.querySelector('#productsTable tbody').innerHTML = rows || '<tr><td colspan="8">Товаров пока нет.</td></tr>';
    document.querySelectorAll('[data-edit]').forEach(btn => btn.addEventListener('click', () => editProduct(btn.dataset.edit)));
    document.querySelectorAll('[data-delete]').forEach(btn => btn.addEventListener('click', () => deleteProduct(btn.dataset.delete)));
    renderPagination('productsPagination', loadProductsAdmin);
}

async function editProduct(id) {
    const form = document.getElementById('productForm');
    form.reset();
    document.getElementById('productId').value = '';
    document.getElementById('productModalTitle').textContent = id ? 'Редактирование товара' : 'Новый товар';

    const preview = document.getElementById('productImagePreview');
    if (preview) preview.innerHTML = '';

    if (id) {
        const product = await apiGet(`/products/${id}`);
        document.getElementById('productId').value = product.id;
        form.name.value = product.name || '';
        form.sellingPrice.value = product.sellingPrice || '';
        form.categoryId.value = product.category?.id || '';
        form.brandId.value = product.brand?.id || '';
        form.sku.value = product.sku || '';
        form.oemNumber.value = product.oemNumber || '';
        const imageUrl = getProductImage(product);
        if (preview) preview.innerHTML = imageUrl ? `<img src="${esc(imageUrl)}" style="max-width:140px;" alt={product.name}>` : '';
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

    const saved = id ? await apiPut(`/products/${id}`, payload) : await apiPost('/products', payload);
    // if a file was selected, upload it
    try {
        const fileInput = form.imageFile;
        if (fileInput && fileInput.files && fileInput.files.length > 0) {
            const fd = new FormData();
            fd.append('file', fileInput.files[0]);
            const resp = await fetch(`/api/products/${saved.id}/image`, {
                method: 'POST',
                headers: getAuthHeaders(false),
                body: fd
            });
            if (!resp.ok) throw new Error('Image upload failed');
        }
    } catch (e) {
        console.error(e);
        alert('Не удалось загрузить изображение');
    }

    closeModal('productEditModal');
    await loadProductsAdmin();
}

async function deleteProduct(id) {
    if (!confirm('Удалить товар?')) return;
    await apiDelete(`/products/${id}`);
    await loadProductsAdmin();
}

async function initCart() {
    await initShell();
    await refreshCartAvailability();
    renderCart();
    document.getElementById('checkoutForm').addEventListener('submit', submitOrder);
    document.getElementById('openCheckout').addEventListener('click', () => {
        fillDeliveryAddressFields(state.currentUser?.deliveryAddress || '');
        renderCheckoutSummary();
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

    total.textContent = money(getCartTotals().total);
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
        renderCheckoutSummary();
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
        renderCheckoutSummary();
    }));
    box.querySelectorAll('[data-remove]').forEach(btn => btn.addEventListener('click', () => {
        removeFromCart(btn.dataset.remove);
        renderCart();
        renderCheckoutSummary();
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
        deliveryAddress: collectDeliveryAddress(),
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
    document.getElementById('ordersDateFrom')?.addEventListener('change', () => {
        state.page = 0;
        loadOrders();
    });
    document.getElementById('ordersDateTo')?.addEventListener('change', () => {
        state.page = 0;
        loadOrders();
    });
    await loadOrders();
}

async function loadOrders() {
    const page = await apiGet(`/orders?${pageParam()}&otherUsers=${showAllOrders}`);
    if (!applyPageInfo(page, loadOrders)) return;
    const adminViewingOtherUsers = state.currentUser.role === 'ADMIN' && showAllOrders;
    const dateFrom = document.getElementById('ordersDateFrom')?.value;
    const dateTo = document.getElementById('ordersDateTo')?.value;
    const orders = getPageContent(page)
        .filter(order => {
            if (!dateFrom && !dateTo) return true;
            const purchaseDate = order.dateOfPurchase ? new Date(order.dateOfPurchase) : null;
            if (!purchaseDate) return false;
            if (dateFrom && purchaseDate < new Date(`${dateFrom}T00:00:00`)) return false;
            return !(dateTo && purchaseDate > new Date(`${dateTo}T23:59:59`));

        });
    const toggle = document.getElementById('toggleAllOrders');
    const hint = document.getElementById('ordersPageHint');

    if (toggle) {
        toggle.textContent = showAllOrders ? 'Мои заказы' : 'Заказы других пользователей';
    }
    if (hint) {
        hint.textContent = adminViewingOtherUsers ? 'Заказы других пользователей с управлением статусами.' : 'История оформленных заказов.';
    }

    document.querySelector('#ordersTable tbody').innerHTML = orders.map(order => `
        <tr>
            <td>#${order.id}</td>
            <td>
                ${adminViewingOtherUsers ? `
                    <select class="status-select" data-status-order="${order.id}">
                        ${orderStatusOptions.map(status => `<option value="${status}" ${status === order.orderStatus ? 'selected' : ''}>${esc(orderStatusText(status))}</option>`).join('')}
                    </select>
                    <button class="btn secondary" data-save-status="${order.id}">Сохранить</button>
                ` : `<span class="badge ${order.orderStatus === 'CREATED' ? 'warn' : 'ok'}">${esc(orderStatusText(order.orderStatus))}</span>`}
            </td>
            <td>
                <strong>${money(order.totalPrice)}</strong>
                <div class="meta">Товары: ${money(order.subtotal)}</div>
                <div class="meta">${esc(order.discountType || 'Скидка')}: ${percent(order.discountPercent)} (${money(order.discountAmount)})</div>
                <div class="meta">Налог: ${percent(order.taxPercent || TAX_PERCENT)} (${money(order.taxAmount)})</div>
            </td>
            <td>${shortDate(order.dateOfPurchase)}</td>
            <td>${shortDate(order.dateOfDelivery)}</td>
            <td>${esc(order.deliveryAddress || '-')}${adminViewingOtherUsers ? `<div class="meta">Пользователь ID: ${esc(order.userId)}</div>` : ''}</td>
            <td><button class="btn secondary" data-receipt="${order.id}">Скачать чек</button></td>
        </tr>
    `).join('') || '<tr><td colspan="7">Заказов пока нет.</td></tr>';

    document.querySelectorAll('[data-save-status]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const select = document.querySelector(`[data-status-order="${btn.dataset.saveStatus}"]`);
            await apiPut(`/orders/${btn.dataset.saveStatus}/status?status=${encodeURIComponent(select.value)}`, null);
            await loadOrders();
        });
    });
    document.querySelectorAll('[data-receipt]').forEach(btn => {
        btn.addEventListener('click', () => downloadReceipt(btn.dataset.receipt));
    });
    renderPagination('ordersPagination', loadOrders);
}

async function downloadReceipt(orderId) {
    const response = await fetch(`/api/orders/${orderId}/receipt`, {
        method: 'GET',
        headers: getAuthHeaders(false)
    });

    if (!response.ok) {
        alert('Не удалось скачать чек');
        return;
    }

    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `receipt-${orderId}.pdf`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
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
            await loadSupplies();
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
    await loadSupplies();
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
    await loadWarehouses();
}

async function deleteWarehouse(id) {
    if (!confirm('Удалить склад?')) return;
    await apiDelete(`/warehouses/${id}`);
    await loadWarehouses();
}

async function initCategories() {
    await initShell(true);
    document.getElementById('createCategory').addEventListener('click', () => editCategory());
    document.getElementById('categoryForm').addEventListener('submit', saveCategory);
    await loadCategoriesAdmin();
}

async function loadCategoriesAdmin() {
    const categories = await apiGet('/categories');
    document.querySelector('#categoriesTable tbody').innerHTML = categories.map(category => `
        <tr>
            <td>${category.id}</td>
            <td><strong>${esc(category.name)}</strong></td>
            <td class="actions">
                <button class="btn secondary" data-edit-category="${category.id}">Изменить</button>
                <button class="btn danger" data-delete-category="${category.id}">Удалить</button>
            </td>
        </tr>
    `).join('') || '<tr><td colspan="3">Категорий пока нет.</td></tr>';
    document.querySelectorAll('[data-edit-category]').forEach(btn => btn.addEventListener('click', () => editCategory(btn.dataset.editCategory)));
    document.querySelectorAll('[data-delete-category]').forEach(btn => btn.addEventListener('click', () => deleteCategory(btn.dataset.deleteCategory)));
}

async function editCategory(id) {
    const form = document.getElementById('categoryForm');
    form.reset();
    document.getElementById('categoryId').value = '';
    document.getElementById('categoryModalTitle').textContent = id ? 'Редактирование категории' : 'Новая категория';

    if (id) {
        const category = await apiGet(`/categories/${id}`);
        document.getElementById('categoryId').value = category.id;
        form.name.value = category.name || '';
    }

    openModal('categoryModal');
}

async function saveCategory(event) {
    event.preventDefault();
    const form = event.target;
    const id = document.getElementById('categoryId').value;
    const payload = { name: form.name.value.trim() };

    if (id) await apiPut(`/categories/${id}`, payload);
    else await apiPost('/categories', payload);

    closeModal('categoryModal');
    await loadCategoriesAdmin();
}

async function deleteCategory(id) {
    if (!confirm('Удалить категорию?')) return;
    await apiDelete(`/categories/${id}`);
    await loadCategoriesAdmin();
}

async function initBrands() {
    await initShell(true);
    document.getElementById('createBrand').addEventListener('click', () => editBrand());
    document.getElementById('brandForm').addEventListener('submit', saveBrand);
    await loadBrandsAdmin();
}

async function loadBrandsAdmin() {
    const brands = await apiGet('/brands');
    document.querySelector('#brandsTable tbody').innerHTML = brands.map(brand => `
        <tr>
            <td>${brand.id}</td>
            <td><strong>${esc(brand.name)}</strong></td>
            <td>${esc(brand.country || '-')}</td>
            <td class="actions">
                <button class="btn secondary" data-edit-brand="${brand.id}">Изменить</button>
                <button class="btn danger" data-delete-brand="${brand.id}">Удалить</button>
            </td>
        </tr>
    `).join('') || '<tr><td colspan="4">Брендов пока нет.</td></tr>';
    document.querySelectorAll('[data-edit-brand]').forEach(btn => btn.addEventListener('click', () => editBrand(btn.dataset.editBrand)));
    document.querySelectorAll('[data-delete-brand]').forEach(btn => btn.addEventListener('click', () => deleteBrand(btn.dataset.deleteBrand)));
}

async function editBrand(id) {
    const form = document.getElementById('brandForm');
    form.reset();
    document.getElementById('brandId').value = '';
    document.getElementById('brandModalTitle').textContent = id ? 'Редактирование бренда' : 'Новый бренд';

    if (id) {
        const brand = await apiGet(`/brands/${id}`);
        document.getElementById('brandId').value = brand.id;
        form.name.value = brand.name || '';
        form.country.value = brand.country || '';
    }

    openModal('brandModal');
}

async function saveBrand(event) {
    event.preventDefault();
    const form = event.target;
    const id = document.getElementById('brandId').value;
    const payload = {
        name: form.name.value.trim(),
        country: form.country.value.trim()
    };

    if (id) await apiPut(`/brands/${id}`, payload);
    else await apiPost('/brands', payload);

    closeModal('brandModal');
    await loadBrandsAdmin();
}

async function deleteBrand(id) {
    if (!confirm('Удалить бренд?')) return;
    await apiDelete(`/brands/${id}`);
    await loadBrandsAdmin();
}

async function initSuppliersAdmin() {
    await initShell(true);
    document.getElementById('createSupplier').addEventListener('click', () => editSupplierRecord());
    document.getElementById('supplierForm').addEventListener('submit', saveSupplierRecord);
    await loadSuppliersAdmin();
}

async function loadSuppliersAdmin() {
    const suppliers = await apiGet('/suppliers');
    document.querySelector('#suppliersTable tbody').innerHTML = suppliers.map(supplier => `
        <tr>
            <td>${supplier.id}</td>
            <td><strong>${esc(supplier.name)}</strong></td>
            <td>${esc(supplier.phone || '-')}</td>
            <td class="actions">
                <button class="btn secondary" data-edit-supplier="${supplier.id}">Изменить</button>
                <button class="btn danger" data-delete-supplier="${supplier.id}">Удалить</button>
            </td>
        </tr>
    `).join('') || '<tr><td colspan="4">Поставщиков пока нет.</td></tr>';
    document.querySelectorAll('[data-edit-supplier]').forEach(btn => btn.addEventListener('click', () => editSupplierRecord(btn.dataset.editSupplier)));
    document.querySelectorAll('[data-delete-supplier]').forEach(btn => btn.addEventListener('click', () => deleteSupplierRecord(btn.dataset.deleteSupplier)));
}

async function editSupplierRecord(id) {
    const form = document.getElementById('supplierForm');
    form.reset();
    document.getElementById('supplierRecordId').value = '';
    document.getElementById('supplierModalTitle').textContent = id ? 'Редактирование поставщика' : 'Новый поставщик';

    if (id) {
        const supplier = await apiGet(`/suppliers/${id}`);
        document.getElementById('supplierRecordId').value = supplier.id;
        form.name.value = supplier.name || '';
        form.phone.value = supplier.phone || '';
    }

    openModal('supplierModal');
}

async function saveSupplierRecord(event) {
    event.preventDefault();
    const form = event.target;
    const id = document.getElementById('supplierRecordId').value;
    const payload = {
        name: form.name.value.trim(),
        phone: form.phone.value.trim()
    };

    if (id) await apiPut(`/suppliers/${id}`, payload);
    else await apiPost('/suppliers', payload);

    closeModal('supplierModal');
    await loadSuppliersAdmin();
}

async function deleteSupplierRecord(id) {
    if (!confirm('Удалить поставщика?')) return;
    await apiDelete(`/suppliers/${id}`);
    await loadSuppliersAdmin();
}

function showMessage(id, text) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = text;
    el.hidden = false;
}

function currentPasswordFromStorage() {
    const credentials = getCredentials();
    if (!credentials) return '';
    return credentials.split(':').slice(1).join(':');
}

async function initProfile() {
    await initShell();
    const form = document.getElementById('profileForm');
    const passwordForm = document.getElementById('passwordForm');

    form.username.value = state.currentUser.username || '';
    form.email.value = state.currentUser.email || '';
    fillDeliveryAddressFields(state.currentUser.deliveryAddress || '', form);
    form.companyName.value = state.currentUser.companyName || '';
    form.phone.value = state.currentUser.phone || '';

    form.addEventListener('submit', async event => {
        event.preventDefault();
        const payload = {
            username: form.username.value.trim(),
            password: null,
            email: form.email.value.trim(),
            deliveryAddress: collectDeliveryAddress(form),
            companyName: form.companyName.value.trim(),
            phone: form.phone.value.trim(),
            role: null,
            priceLevelId: null
        };

        const updated = await apiPut('/users/me', payload);
        state.currentUser = updated;
        setCredentials(updated.username, currentPasswordFromStorage());
        showMessage('profileMessage', 'Данные сохранены.');
    });

    passwordForm.addEventListener('submit', async event => {
        event.preventDefault();
        const oldPassword = passwordForm.oldPassword.value;
        const newPassword = passwordForm.newPassword.value;

        await apiPost('/users/me/change-password', { oldPassword, newPassword });
        setCredentials(state.currentUser.username, newPassword);
        passwordForm.reset();
        showMessage('passwordMessage', 'Пароль изменен.');
    });
}

async function initUsers() {
    await initShell(true);
    state.size = 20;
    document.getElementById('createUser').addEventListener('click', () => editUser());
    document.getElementById('managePriceLevels').addEventListener('click', () => openPriceLevelModal());
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
    const pwField = document.getElementById('passwordField');
    if (pwField) pwField.style.display = id ? 'none' : '';
    document.getElementById('userModalTitle').textContent = id ? 'Редактирование пользователя' : 'Новый пользователь';

    // Загружаем список уровней цен
    const priceLevels = await apiGet('/users/price-levels/all');
    const priceLevelSelect = form.priceLevelId;
    priceLevelSelect.innerHTML = '<option value="">-- Выберите уровень цены --</option>';
    priceLevels.forEach(level => {
        const option = document.createElement('option');
        option.value = level.id;
        option.textContent = `${level.name} (x${level.ratio})`;
        priceLevelSelect.appendChild(option);
    });

    if (id) {
        const user = await apiGet(`/users/${id}`);
        document.getElementById('userId').value = user.id;
        form.username.value = user.username || '';
        form.email.value = user.email || '';
        fillDeliveryAddressFields(user.deliveryAddress || '', form);
        form.companyName.value = user.companyName || '';
        form.phone.value = user.phone || '';
        form.role.value = user.role || 'USER';
        form.priceLevelId.value = user.priceLevelId || '';
    }

    openModal('userModal');
}

// Price level management
async function openPriceLevelModal() {
    // create modal if not exists
    if (!document.getElementById('priceLevelModal')) {
        const modal = document.createElement('div');
        modal.className = 'modal';
        modal.id = 'priceLevelModal';
        modal.innerHTML = `
            <div class="modal__content">
                <div class="modal__head"><h2>Уровни цен</h2><button class="btn secondary" data-close-modal="priceLevelModal">Закрыть</button></div>
                <div style="padding:16px">
                    <div id="priceLevelList"></div>
                    <hr>
                    <form id="priceLevelForm">
                        <input id="priceLevelId" type="hidden">
                        <div class="field"><label>Название</label><input name="name" required></div>
                        <div class="field"><label>Коэффициент</label><input name="ratio" type="number" step="0.01" min="0" required></div>
                        <div class="actions"><button class="btn" type="submit">Сохранить</button></div>
                    </form>
                </div>
            </div>`;
        document.body.appendChild(modal);
        document.getElementById('priceLevelForm').addEventListener('submit', savePriceLevel);
    }

    document.getElementById('priceLevelId').value = '';
    document.getElementById('priceLevelForm').reset();
    openModal('priceLevelModal');
    await loadPriceLevels();
}

async function loadPriceLevels() {
    const listEl = document.getElementById('priceLevelList');
    listEl.innerHTML = 'Загрузка...';
    const levels = await apiGet('/users/price-levels/all');
    if (!levels || !levels.length) {
        listEl.innerHTML = '<div>Уровней цен нет.</div>';
        return;
    }
    listEl.innerHTML = levels.map(l => `<div style="display:flex;align-items:center;justify-content:space-between;padding:6px 0"><div>${esc(l.name)} (x${l.ratio})</div><div><button class="btn secondary" data-edit-pricelevel="${l.id}">Изменить</button> <button class="btn danger" data-delete-pricelevel="${l.id}">Удалить</button></div></div>`).join('');
    listEl.querySelectorAll('[data-edit-pricelevel]').forEach(btn => btn.addEventListener('click', async () => {
        const id = btn.dataset.editPricelevel;
        const pl = levels.find(x => x.id === id);
        document.getElementById('priceLevelId').value = pl.id;
        const form = document.getElementById('priceLevelForm');
        form.name.value = pl.name;
        form.ratio.value = pl.ratio;
    }));
    listEl.querySelectorAll('[data-delete-pricelevel]').forEach(btn => btn.addEventListener('click', async () => {
        if (!confirm('Удалить уровень цены?')) return;
        await apiDelete(`/users/price-levels/${btn.dataset.deletePricelevel}`);
        await loadPriceLevels();
    }));
}

async function savePriceLevel(event) {
    event.preventDefault();
    const form = event.target;
    const id = document.getElementById('priceLevelId').value;
    const payload = {
        name: form.name.value.trim(),
        ratio: Number(form.ratio.value)
    };
    if (id) await apiPut(`/users/price-levels/${id}`, payload);
    else await apiPost('/users/price-levels', payload);
    form.reset();
    await loadPriceLevels();
}

async function saveUser(event) {
    event.preventDefault();
    const form = event.target;
    const id = document.getElementById('userId').value;
    const payload = {
        username: form.username.value.trim(),
        password: form.password.value || null,
        email: form.email.value.trim(),
        deliveryAddress: collectDeliveryAddress(form),
        companyName: form.companyName.value.trim(),
        phone: form.phone.value.trim(),
        role: form.role.value,
        priceLevelId: form.priceLevelId.value ? Number(form.priceLevelId.value) : null
    };
    if (id) await apiPut(`/users/${id}`, payload);
    else await apiPost('/users', payload);
    closeModal('userModal');
    await loadUsers();
}

async function deleteUser(id) {
    if (!confirm('Удалить пользователя?')) return;
    await apiDelete(`/users/${id}`);
    await loadUsers();
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
        profile: initProfile,
        supplies: initSupplies,
        warehouses: initWarehouses,
        categories: initCategories,
        brands: initBrands,
        suppliers: initSuppliersAdmin,
        users: initUsers
    }[page];

    if (init) {
        init().catch(err => {
            console.error(err);
            alert(err.message || 'Ошибка выполнения операции');
        });
    }
});
