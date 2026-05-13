const API_BASE = '/api';
const AUTH_KEY = 'autoshop.credentials';
const CART_KEY = 'autoshop.cart';

function setCredentials(username, password) {
    localStorage.setItem(AUTH_KEY, `${username}:${password}`);
}

function getCredentials() {
    return localStorage.getItem(AUTH_KEY) || localStorage.getItem('credentials');
}

function getAuthHeaders(json = true) {
    const credentials = getCredentials();
    const headers = {};

    if (credentials) {
        headers.Authorization = `Basic ${btoa(unescape(encodeURIComponent(credentials)))}`;
    }

    if (json) {
        headers['Content-Type'] = 'application/json';
    }

    return headers;
}

async function request(path, options = {}) {
    const response = await fetch(API_BASE + path, {
        ...options,
        headers: {
            ...getAuthHeaders(options.body !== undefined),
            ...(options.headers || {})
        }
    });

    if (response.status === 401 || response.status === 403) {
        throw new Error(response.status === 401 ? 'Нужно войти в систему' : 'Недостаточно прав');
    }

    if (!response.ok) {
        let message = `HTTP ${response.status}`;
        try {
            const data = await response.json();
            message = data.message || data.error || message;
        } catch (ignored) {
            message = response.statusText || message;
        }
        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

function apiGet(path) {
    return request(path, { method: 'GET' });
}

function apiPost(path, data) {
    return request(path, { method: 'POST', body: JSON.stringify(data) });
}

function apiPut(path, data) {
    const options = { method: 'PUT' };
    if (data !== null && data !== undefined) {
        options.body = JSON.stringify(data);
    }
    return request(path, options);
}

function apiDelete(path) {
    return request(path, { method: 'DELETE' });
}

function isLoggedIn() {
    return Boolean(getCredentials());
}

function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = 'login.html';
    }
}

function logout() {
    localStorage.removeItem(AUTH_KEY);
    localStorage.removeItem('credentials');
    window.location.href = 'login.html';
}

async function getCurrentUser() {
    if (!isLoggedIn()) return null;
    return apiGet('/users/me');
}

function getCart() {
    return JSON.parse(localStorage.getItem(CART_KEY) || localStorage.getItem('cart') || '[]');
}

function saveCart(cart) {
    localStorage.setItem(CART_KEY, JSON.stringify(cart));
    localStorage.removeItem('cart');
    updateCartCount();
}

function addToCart(product, quantity = 1) {
    const cart = getCart();
    const existing = cart.find(item => Number(item.id) === Number(product.id));
    const stockKnown = Array.isArray(product.stocks);
    const availableQuantity = Array.isArray(product.stocks)
        ? product.stocks.reduce((sum, stock) => sum + Math.max(Number(stock.quantity || 0), 0), 0)
        : Number(product.availableQuantity || 0);
    const maxQuantity = stockKnown ? availableQuantity : null;

    if (maxQuantity !== null && maxQuantity <= 0) {
        return false;
    }

    if (existing) {
        existing.availableQuantity = maxQuantity ?? existing.availableQuantity ?? existing.quantity;
        existing.quantity = Math.min(existing.quantity + quantity, existing.availableQuantity);
    } else {
        cart.push({
            id: product.id,
            name: product.name,
            sku: product.sku || '',
            brand: product.brand && product.brand.name ? product.brand.name : '',
            price: Number(product.sellingPrice || product.price || 0),
            quantity: Math.min(quantity, maxQuantity ?? quantity),
            availableQuantity: maxQuantity ?? availableQuantity
        });
    }

    saveCart(cart);
    return true;
}

function removeFromCart(productId) {
    saveCart(getCart().filter(item => Number(item.id) !== Number(productId)));
}

function updateCartItem(productId, quantity) {
    const nextQuantity = Number(quantity);
    const cart = getCart()
        .map(item => {
            if (Number(item.id) !== Number(productId)) {
                return item;
            }

            const max = Number(item.availableQuantity || nextQuantity);
            return { ...item, quantity: Math.min(Math.max(nextQuantity, 0), max) };
        })
        .filter(item => item.quantity > 0);
    saveCart(cart);
}

function clearCart() {
    saveCart([]);
}

function getCartTotal() {
    return getCart().reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0);
}

function updateCartCount() {
    const badge = document.querySelector('[data-cart-count]');
    if (!badge) return;

    const count = getCart().reduce((sum, item) => sum + Number(item.quantity || 0), 0);
    badge.textContent = count;
    badge.hidden = count === 0;
}
