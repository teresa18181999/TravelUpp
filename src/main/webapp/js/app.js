const API = '/api';

const $ = id => document.getElementById(id);

let currentUser = null;
let myTrips = [];
let currentTripId = null;
let currentDay = 1;
let currentActivities = [];
let contactos = [];
let adminClickCount = 0;
let adminPanelOrigin = null;
let pendingPhone = null;
let pendingAction = null;
let editParticipants = [];

function escapeHtml(t) {
  const d = document.createElement('div');
  d.textContent = t;
  return d.innerHTML;
}

function saveAppState() {
  const tab = document.querySelector('.tab-item.active');
  const state = {
    screen: document.querySelector('.screen.active')?.id || null,
    tab: tab?.dataset?.tab || 'current-trips',
    tripId: currentTripId,
    day: currentDay
  };
  try { sessionStorage.setItem('appState', JSON.stringify(state)); } catch {}
  try { localStorage.setItem('appState', JSON.stringify(state)); } catch {}
}

window.addEventListener('beforeunload', saveAppState);

function restoreAppState() {
  try {
    let raw = sessionStorage.getItem('appState');
    if (!raw) raw = localStorage.getItem('appState');
    return raw ? JSON.parse(raw) : null;
  } catch { return null; }
}

async function autoLogin(phone) {
  try {
    const r1 = await fetch(`${API}/login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ telefono: phone }) });
    const d1 = await r1.json();
    if (!d1.ok) return false;
    const code = phone === '618941168' ? '442255' : '123456';
    const r2 = await fetch(`${API}/verify-login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ telefono: phone, codigo: code }) });
    const d2 = await r2.json();
    if (!d2.ok) return false;
    const r3 = await fetch(`${API}/perfil`);
    const d3 = await r3.json();
    if (!d3.ok) return false;
    currentUser = d3.perfil;
    document.getElementById('settings-admin-group').style.display = currentUser.esAdmin ? 'block' : 'none';
    const saved = restoreAppState();
    showMainApp();
    if (saved && saved.screen === 'trip-detail' && saved.tripId) {
      await openTripDetail(saved.tripId);
      if (saved.day) currentDay = saved.day;
    } else if (saved && saved.tab) {
      const tabEl = document.querySelector(`.tab-item[data-tab="${saved.tab}"]`);
      if (tabEl) tabEl.click();
    }
    return true;
  } catch { return false; }
}

function showScreen(id) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById(id).classList.add('active');
}

function showError(el, msg) {
  el.textContent = msg;
  el.style.display = 'block';
}

function hideError(el) {
  el.style.display = 'none';
}

function showSuccess(el, msg) {
  el.textContent = msg;
  el.style.display = 'block';
}

function hideSuccess(el) {
  el.style.display = 'none';
}

function formatDate(inicio, fin) {
  const meses = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic'];
  const fi = new Date(inicio + 'T00:00:00');
  const ff = new Date(fin + 'T00:00:00');
  return `${fi.getDate()} ${meses[fi.getMonth()]} - ${ff.getDate()} ${meses[ff.getMonth()]} ${ff.getFullYear()}`;
}

function getBadgeClass(estado) {
  const m = { 'En preparación': 'preparacion', 'Confirmado': 'confirmado', 'En curso': 'curso', 'Completado': 'completado' };
  return m[estado] || 'preparacion';
}

function daysBetween(a, b) {
  const fa = new Date(a + 'T00:00:00');
  const fb = new Date(b + 'T00:00:00');
  return Math.max(1, Math.floor((fb - fa) / 86400000) + 1);
}

// ===== SCREEN NAVIGATION =====
function showMainApp() {
  document.getElementById('main-app').classList.remove('hidden');
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById('main-app').classList.add('active');
  loadMyTrips();
  loadContactosFromServer();
  loadInvitacionesBadge();
}

// ===== AUTH =====
async function checkSession() {
  try {
    const r = await fetch(`${API}/perfil`);
    const data = await r.json();
    if (data.ok) {
      currentUser = data.perfil;
      document.getElementById('settings-admin-group').style.display = currentUser.esAdmin ? 'block' : 'none';
      const saved = restoreAppState();
      if (saved && saved.screen === 'trip-detail' && saved.tripId) {
        showMainApp();
        await openTripDetail(saved.tripId);
        if (saved.day) currentDay = saved.day;
      } else {
        showMainApp();
        if (saved && saved.tab) {
          const tabEl = document.querySelector(`.tab-item[data-tab="${saved.tab}"]`);
          if (tabEl) tabEl.click();
        }
      }
      return;
    }
  } catch {}
  const storedPhone = (() => { try { return localStorage.getItem('rememberPhone'); } catch { return null; } })();
  if (storedPhone) {
    const ok = await autoLogin(storedPhone);
    if (ok) return;
  }
  showScreen('login-screen');
}

document.getElementById('login-form').addEventListener('submit', async e => {
  e.preventDefault();
  const tel = document.getElementById('login-phone').value.replace(/\s/g, '');
  const errEl = document.getElementById('login-error');
  hideError(errEl);
  if (!tel) { showError(errEl, 'Introduce un número de teléfono'); return; }
  try {
    const r = await fetch(`${API}/login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ telefono: tel }) });
    const data = await r.json();
    if (data.ok) {
      pendingPhone = tel;
      pendingAction = 'login';
      document.getElementById('code-input').value = '';
      hideError(document.getElementById('code-error'));
      showScreen('code-screen');
    } else {
      showError(errEl, data.error || 'Error al iniciar sesión');
    }
  } catch (err) {
    showError(errEl, 'Error de conexión');
  }
});

document.getElementById('register-form').addEventListener('submit', async e => {
  e.preventDefault();
  const tel = document.getElementById('register-phone').value.replace(/\s/g, '');
  const name = document.getElementById('register-name').value.trim();
  const errEl = document.getElementById('register-error');
  hideError(errEl);
  if (!tel || !name) { showError(errEl, 'Completa todos los campos'); return; }
  if (tel.length !== 9 || !/^\d{9}$/.test(tel)) { showError(errEl, 'El número de teléfono no es valido'); return; }
  try {
    const r = await fetch(`${API}/register`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ telefono: tel, nombre: name }) });
    const data = await r.json();
    if (data.ok) {
      pendingPhone = tel;
      pendingAction = 'register';
      document.getElementById('code-input').value = '';
      hideError(document.getElementById('code-error'));
      showScreen('code-screen');
    } else {
      showError(errEl, data.error || 'Error al registrarse');
    }
  } catch {
    showError(errEl, 'Error de conexión');
  }
});

document.getElementById('code-form').addEventListener('submit', async e => {
  e.preventDefault();
  const code = document.getElementById('code-input').value.replace(/-/g, '');
  const errEl = document.getElementById('code-error');
  hideError(errEl);
  if (!code || !pendingPhone) { showError(errEl, 'Código inválido'); return; }
  const endpoint = pendingAction === 'login' ? 'verify-login' : 'verify-register';
  try {
    const r = await fetch(`${API}/${endpoint}`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ telefono: pendingPhone, codigo: code }) });
    const data = await r.json();
    if (data.ok) {
      if (document.getElementById('keep-session').checked) {
        try { localStorage.setItem('rememberPhone', pendingPhone); } catch {}
      } else {
        try { localStorage.removeItem('rememberPhone'); } catch {}
      }
      pendingPhone = null;
      pendingAction = null;
      const r2 = await fetch(`${API}/perfil`);
      const perfilData = await r2.json();
      if (perfilData.ok) {
        currentUser = perfilData.perfil;
        document.getElementById('settings-admin-group').style.display = currentUser.esAdmin ? 'block' : 'none';
      }
      showMainApp();
    } else {
      showError(errEl, data.error || 'Código incorrecto');
    }
  } catch {
    showError(errEl, 'Error de conexión');
  }
});

const PHONE_INPUTS = ['login-phone', 'register-phone'];
PHONE_INPUTS.forEach(id => {
  document.getElementById(id).addEventListener('input', function () {
    let digits = this.value.replace(/\D/g, '').slice(0, 9);
    let formatted = '';
    for (let i = 0; i < digits.length; i++) {
      if (i > 0 && i % 3 === 0) formatted += ' ';
      formatted += digits[i];
    }
    this.value = formatted;
  });
});

document.getElementById('code-input').addEventListener('input', function () {
  let digits = this.value.replace(/\D/g, '').slice(0, 6);
  let formatted = '';
  for (let i = 0; i < digits.length; i++) {
    if (i === 3) formatted += '-';
    formatted += digits[i];
  }
  this.value = formatted;
});

document.getElementById('go-to-register').addEventListener('click', () => {
  hideError(document.getElementById('login-error'));
  showScreen('register-screen');
});

document.getElementById('go-to-login').addEventListener('click', () => {
  hideError(document.getElementById('register-error'));
  showScreen('login-screen');
});

document.getElementById('resend-code').addEventListener('click', async () => {
  if (!pendingPhone) return;
  const endpoint = pendingAction === 'login' ? 'login' : 'register';
  await fetch(`${API}/${endpoint}`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ telefono: pendingPhone }) });
  showError(document.getElementById('code-error'), 'Código reenviado');
});

document.getElementById('btn-back-to-auth').addEventListener('click', () => {
  hideError(document.getElementById('code-error'));
  const target = pendingAction === 'register' ? 'register-screen' : 'login-screen';
  hideError(document.getElementById('login-error'));
  hideError(document.getElementById('register-error'));
  pendingPhone = null;
  pendingAction = null;
  showScreen(target);
});

// ===== LOGOUT =====
document.getElementById('btn-logout').addEventListener('click', async () => {
  try { await fetch(`${API}/logout`, { method: 'POST' }); } catch {}
  try { localStorage.removeItem('rememberPhone'); } catch {}
  try { localStorage.removeItem('appState'); } catch {}
  try { sessionStorage.removeItem('appState'); } catch {}
  currentUser = null;
  myTrips = [];
  currentTripId = null;
  currentDay = 1;
  currentActivities = [];
  document.getElementById('main-app').classList.add('hidden');
  document.getElementById('settings-admin-group').style.display = 'none';
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  showScreen('login-screen');
});

// ===== CONFIRM MODAL =====
function showConfirm(message) {
  return new Promise(resolve => {
    const modal = document.getElementById('confirm-modal');
    document.getElementById('confirm-text').textContent = message;
    modal.style.display = 'flex';
    const ok = document.getElementById('confirm-ok');
    const cancel = document.getElementById('confirm-cancel');
    const backdrop = document.getElementById('confirm-backdrop');
    const cleanup = r => { modal.style.display = 'none'; resolve(r); };
    ok.onclick = () => cleanup(true);
    cancel.onclick = () => cleanup(false);
    backdrop.onclick = () => cleanup(false);
  });
}

// ===== DELETE ACCOUNT =====
document.getElementById('btn-delete-account').addEventListener('click', async () => {
  if (!await showConfirm('¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.')) return;
  try {
    const r = await fetch(`${API}/perfil`, { method: 'DELETE' });
    const data = await r.json();
    if (data.ok) {
      try { localStorage.removeItem('rememberPhone'); } catch {}
      try { localStorage.removeItem('appState'); } catch {}
      try { sessionStorage.removeItem('appState'); } catch {}
      currentUser = null;
      myTrips = [];
      currentTripId = null;
      currentDay = 1;
      currentActivities = [];
      document.getElementById('main-app').classList.add('hidden');
      document.getElementById('settings-admin-group').style.display = 'none';
      document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
      showScreen('login-screen');
    } else {
      alert(data.error || 'Error al eliminar la cuenta');
    }
  } catch {
    alert('Error de conexión al eliminar la cuenta');
  }
});

// ===== TABS =====
document.querySelectorAll('.tab-item').forEach(tab => {
  tab.addEventListener('click', () => {
    document.querySelectorAll('.tab-item').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    tab.classList.add('active');
    document.getElementById(`tab-${tab.dataset.tab}`).classList.add('active');
    saveAppState();
  });
});

// ===== NEW TRIP MODE SELECTOR =====
document.getElementById('btn-mode-manual').addEventListener('click', () => {
  document.getElementById('new-trip-selector').classList.add('hidden');
  document.getElementById('new-trip-manual').classList.remove('hidden');
});

document.getElementById('btn-mode-auto').addEventListener('click', () => {
  document.getElementById('new-trip-selector').classList.add('hidden');
  document.getElementById('new-trip-auto').classList.remove('hidden');
});

document.getElementById('btn-back-to-modes').addEventListener('click', () => {
  document.getElementById('new-trip-manual').classList.add('hidden');
  document.getElementById('new-trip-selector').classList.remove('hidden');
});

document.getElementById('btn-back-to-modes-from-auto').addEventListener('click', () => {
  document.getElementById('new-trip-auto').classList.add('hidden');
  document.getElementById('new-trip-selector').classList.remove('hidden');
});

document.getElementById('go-to-new-trip-from-empty').addEventListener('click', () => {
  document.querySelector('.tab-item[data-tab="new-trip"]').click();
});

document.getElementById('go-to-new-trip-from-empty2').addEventListener('click', () => {
  document.querySelector('.tab-item[data-tab="new-trip"]').click();
});

// ===== TRIP COVER =====
document.getElementById('btn-trip-cover').addEventListener('click', () => {
  document.getElementById('trip-cover-file').click();
});

document.getElementById('trip-cover-file').addEventListener('change', e => {
  const file = e.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = ev => {
    document.getElementById('trip-cover').value = ev.target.result;
    const preview = document.getElementById('trip-cover-preview');
    preview.src = ev.target.result;
    preview.classList.remove('hidden');
  };
  reader.readAsDataURL(file);
});

// ===== TAG INPUT =====
const tripParticipants = [];

function removeParticipant(phone) {
  const idx = tripParticipants.findIndex(t => t.phone === phone);
  if (idx >= 0) tripParticipants.splice(idx, 1);
  renderParticipantTags();
}

function renderParticipantTags() {
  const container = document.getElementById('trip-participants-tags');
  container.innerHTML = tripParticipants.map(t => `
    <span class="tag-item">${escapeHtml(t.name)}
      <button type="button" data-phone="${escapeHtml(t.phone)}">×</button>
    </span>
  `).join('');
  container.querySelectorAll('button').forEach(btn => {
    btn.addEventListener('click', () => removeParticipant(btn.dataset.phone));
  });
}

function filterContactos(query) {
  if (!query) return [];
  const q = query.toLowerCase();
  return contactos.filter(c => c.nombre.toLowerCase().includes(q) || c.telefono.includes(q));
}

function initParticipantInput() {
  const input = document.getElementById('trip-participant-input');
  const suggestions = document.getElementById('trip-contact-suggestions');

  input.addEventListener('input', () => {
    const q = input.value.trim();
    const results = filterContactos(q);
    if (results.length === 0 || !q) {
      suggestions.style.display = 'none';
      return;
    }
    suggestions.innerHTML = results.map(c => `
      <div class="contact-suggestion-item" data-phone="${escapeHtml(c.telefono)}" data-name="${escapeHtml(c.nombre)}">
        <span>${escapeHtml(c.nombre)}</span>
        <span style="color:var(--lgray);font-size:.75rem">${escapeHtml(c.telefono)}</span>
      </div>
    `).join('');
    suggestions.style.display = 'block';
    suggestions.querySelectorAll('.contact-suggestion-item').forEach(el => {
      el.addEventListener('click', () => {
        const phone = el.dataset.phone;
        const name = el.dataset.name;
        if (!tripParticipants.find(t => t.phone === phone)) {
          tripParticipants.push({ name, phone });
          renderParticipantTags();
        }
        suggestions.style.display = 'none';
        input.value = '';
      });
    });
  });

  input.addEventListener('blur', () => setTimeout(() => { suggestions.style.display = 'none'; }, 200));
  input.addEventListener('focus', () => {
    if (input.value.trim()) input.dispatchEvent(new Event('input'));
  });

  document.getElementById('btn-add-participant-tag').addEventListener('click', () => {
    const val = input.value.trim();
    if (!val) return;
    if (!tripParticipants.find(t => t.phone === val || t.name === val)) {
      tripParticipants.push({ name: val, phone: val });
      renderParticipantTags();
      input.value = '';
    }
  });
}

initParticipantInput();

// ===== CREATE TRIP =====
document.getElementById('new-trip-form').addEventListener('submit', async e => {
  e.preventDefault();
  const errEl = document.getElementById('new-trip-error');
  hideError(errEl);

  const data = {
    nombre: document.getElementById('trip-name').value.trim(),
    destino: document.getElementById('trip-destination').value.trim(),
    fechaInicio: document.getElementById('trip-start').value,
    fechaFin: document.getElementById('trip-end').value,
    participantes: tripParticipants.map(t => t.phone).join(', '),
    coverImage: document.getElementById('trip-cover').value || '',
    estado: 'En preparación'
  };

  if (!data.nombre || !data.destino || !data.fechaInicio || !data.fechaFin) {
    showError(errEl, 'Completa todos los campos obligatorios');
    return;
  }

  try {
    const r = await fetch(`${API}/crear-viaje`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
    const result = await r.json();
    if (result.ok) {
      document.getElementById('new-trip-form').reset();
      document.getElementById('trip-cover').value = '';
      document.getElementById('trip-cover-preview').classList.add('hidden');
      tripParticipants.length = 0;
      document.getElementById('trip-participants-tags').innerHTML = '';
      document.getElementById('new-trip-manual').classList.add('hidden');
      document.getElementById('new-trip-selector').classList.remove('hidden');
      document.querySelector('.tab-item[data-tab="current-trips"]').click();
      loadMyTrips();
    } else {
      showError(errEl, result.error || 'Error al crear el viaje');
    }
  } catch {
    showError(errEl, 'Error de conexión');
  }
});

// ===== LOAD & RENDER TRIPS =====
async function loadMyTrips() {
  try {
    const r = await fetch(`${API}/listar-viajes`);
    const data = await r.json();
    if (data.ok) {
      myTrips = data.viajes || [];
      renderTrips();
    }
  } catch {}
}

function renderTrips() {
  const myList = document.getElementById('my-trips-list');
  const myEmpty = document.getElementById('my-trips-empty');
  const currentList = document.getElementById('current-trips-list');
  const currentEmpty = document.getElementById('current-trips-empty');

  myList.innerHTML = '';
  currentList.innerHTML = '';

  if (myTrips.length === 0) {
    myEmpty.style.display = 'flex';
    currentEmpty.style.display = 'flex';
    return;
  }

  const completados = myTrips.filter(v => v.estado === 'Completado');
  const activos = myTrips.filter(v => v.estado !== 'Completado');

  if (completados.length === 0) {
    myEmpty.style.display = 'flex';
  } else {
    myEmpty.style.display = 'none';
    completados.forEach(trip => {
      myList.appendChild(createTripCard(trip, false));
    });
  }

  if (activos.length === 0) {
    currentEmpty.style.display = 'flex';
  } else {
    currentEmpty.style.display = 'none';
    activos.forEach(trip => {
      currentList.appendChild(createTripCard(trip, false));
    });
  }
}

function createTripCard(trip, readonly) {
  const div = document.createElement('div');
  div.className = 'trip-card';

  const isCompleted = trip.estado === 'Completado';

  const badgeClass = getBadgeClass(trip.estado);
  const fecha = formatDate(trip.fechaInicio, trip.fechaFin);
  const hasCover = trip.coverImage && trip.coverImage.startsWith('data:');

  let deleteOverlay = '';
  if (isCompleted) {
    deleteOverlay = `<div class="trip-card-delete-overlay">
      <button class="trip-delete-trigger" data-id="${trip.id}">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#dc2626" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
      </button>
      <button class="trip-delete-confirm" data-id="${trip.id}" style="display:none">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#dc2626" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>`;
  }

  let actions = '';
  if (!isCompleted) {
    const mostrarEdit = !readonly;
    const mostrarDelete = !readonly && !trip.invitado;
    const mostrarLeave = !readonly && trip.invitado;
    if (mostrarEdit || mostrarDelete || mostrarLeave) {
      actions = '<div class="trip-card-actions">';
      if (mostrarEdit) {
        actions += `<button class="trip-card-btn trip-card-btn-edit" data-id="${trip.id}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg> Editar</button>`;
      }
      if (mostrarDelete) {
        actions += `<button class="trip-card-btn trip-card-btn-delete" data-id="${trip.id}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg> Eliminar</button>`;
      }
      if (mostrarLeave) {
        actions += `<button class="trip-card-btn trip-card-btn-leave" data-id="${trip.id}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg> Salir del viaje</button>`;
      }
      actions += '</div>';
    }
  }

  div.innerHTML = `
    ${deleteOverlay}
    ${hasCover ? `<img class="trip-card-img" src="${escapeHtml(trip.coverImage)}" alt="">`
      : `<div class="trip-card-placeholder"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M17.8 19.2 16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.5-.1 1 .3 1.3L9 12l-2 3H4l-1 1 3 2 2 3 1-1v-3l3-2 3.5 5.3c.3.4.8.5 1.3.3l.5-.2c.4-.3.6-.7.5-1.2z"/></svg></div>`}
    <div class="trip-card-body">
      <h3 class="trip-card-title">${escapeHtml(trip.nombre)}</h3>
      <div class="trip-card-info">
        <div class="trip-card-info-row">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          <span>${fecha}</span>
        </div>
        <div class="trip-card-info-row">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
          <span>${escapeHtml(trip.destino || 'Sin destino')}</span>
        </div>
      </div>
      ${isCompleted
        ? `<span class="trip-card-badge completado"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg></span>`
        : `<span class="trip-card-badge ${badgeClass}">${escapeHtml(trip.estado)}</span>`}
      ${actions}
    </div>
  `;

  div.querySelector('.trip-card-body').addEventListener('click', e => {
    if (e.target.closest('.trip-card-actions')) return;
    if (e.target.closest('.trip-card-delete-overlay')) return;
    openTripDetail(trip.id);
  });

  if (isCompleted) {
    const trigger = div.querySelector('.trip-delete-trigger');
    const confirmBtn = div.querySelector('.trip-delete-confirm');
    if (trigger && confirmBtn) {
      trigger.addEventListener('click', e => {
        e.stopPropagation();
        trigger.style.display = 'none';
        confirmBtn.style.display = 'flex';
      });
      confirmBtn.addEventListener('click', e => {
        e.stopPropagation();
        deleteTrip(trip.id);
      });
    }
  } else if (!readonly) {
    div.querySelector('.trip-card-btn-edit')?.addEventListener('click', e => {
      e.stopPropagation();
      openTripDetail(trip.id);
    });
    const deleteBtn = div.querySelector('.trip-card-btn-delete');
    if (deleteBtn) {
      deleteBtn.addEventListener('click', e => {
        e.stopPropagation();
        if (confirm('¿Eliminar este viaje?')) deleteTrip(trip.id);
      });
    }
    const leaveBtn = div.querySelector('.trip-card-btn-leave');
    if (leaveBtn) {
      leaveBtn.addEventListener('click', e => {
        e.stopPropagation();
        if (confirm('¿Salir del viaje? Ya no aparecerá en tu lista.')) leaveTrip(trip.id);
      });
    }
  }

  return div;
}

// ===== DELETE TRIP =====
async function deleteTrip(id) {
  try {
    const r = await fetch(`${API}/eliminar-viaje`, { method: 'DELETE', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id }) });
    const data = await r.json();
    if (data.ok) {
      loadMyTrips();
      if (currentTripId === id) {
        document.getElementById('trip-detail').classList.remove('active');
        document.getElementById('main-app').classList.remove('hidden');
        document.getElementById('main-app').classList.add('active');
      }
    }
  } catch {}
}

// ===== LEAVE TRIP (invitado) =====
async function leaveTrip(tripId) {
  try {
    const r = await fetch(`${API}/invitaciones`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ viajeId: tripId, accion: 'salir' })
    });
    const data = await r.json();
    if (data.ok) {
      await loadMyTrips();
      if (currentTripId === tripId) {
        document.getElementById('trip-detail').classList.remove('active');
        document.getElementById('main-app').classList.remove('hidden');
        document.getElementById('main-app').classList.add('active');
      }
    } else {
      alert(data.error || 'Error al salir del viaje');
    }
  } catch (e) {
    alert('Error de conexión al salir del viaje');
  }
}

// ===== EDIT TRIP MODAL =====
let editingTrip = null;

function openEditModal(trip) {
  editingTrip = trip;
  document.getElementById('edit-modal-name').value = trip.nombre;
  document.getElementById('edit-modal-start').value = trip.fechaInicio;
  document.getElementById('edit-modal-end').value = trip.fechaFin;

  editParticipants = (trip.participantes || '').split(',').map(s => s.trim()).filter(Boolean).map(p => {
    const c = contactos.find(co => co.telefono === p);
    return c ? { name: c.nombre, phone: c.telefono } : { name: p, phone: p };
  });
  renderEditParticipantTags();

  document.getElementById('edit-trip-modal').style.display = 'flex';
  document.body.style.overflow = 'hidden';
}

function closeEditModal() {
  document.getElementById('edit-trip-modal').style.display = 'none';
  document.body.style.overflow = '';
  editingTrip = null;
}

function renderEditParticipantTags() {
  const container = document.getElementById('edit-modal-participants');
  container.innerHTML = editParticipants.map(p => `
    <span class="tag-item">${escapeHtml(p.name)}
      <button type="button" data-phone="${escapeHtml(p.phone)}">×</button>
    </span>
  `).join('');
  container.querySelectorAll('button').forEach(btn => {
    btn.addEventListener('click', () => {
      editParticipants = editParticipants.filter(p => p.phone !== btn.dataset.phone);
      renderEditParticipantTags();
    });
  });
}

function initEditParticipantInput() {
  const input = document.getElementById('edit-modal-participant-input');
  const suggestions = document.getElementById('edit-modal-contact-suggestions');

  input.addEventListener('input', () => {
    const q = input.value.trim();
    const results = filterContactos(q);
    if (results.length === 0 || !q) {
      suggestions.style.display = 'none';
      return;
    }
    suggestions.innerHTML = results.map(c => `
      <div class="contact-suggestion-item" data-phone="${escapeHtml(c.telefono)}" data-name="${escapeHtml(c.nombre)}">
        <span>${escapeHtml(c.nombre)}</span>
        <span style="color:var(--lgray);font-size:.75rem">${escapeHtml(c.telefono)}</span>
      </div>
    `).join('');
    suggestions.style.display = 'block';
    suggestions.querySelectorAll('.contact-suggestion-item').forEach(el => {
      el.addEventListener('click', () => {
        const phone = el.dataset.phone;
        const name = el.dataset.name;
        if (phone && !editParticipants.find(p => p.phone === phone)) {
          editParticipants.push({ name, phone });
          renderEditParticipantTags();
        }
        suggestions.style.display = 'none';
        input.value = '';
      });
    });
  });

  input.addEventListener('blur', () => setTimeout(() => { suggestions.style.display = 'none'; }, 200));
  input.addEventListener('focus', () => {
    if (input.value.trim()) input.dispatchEvent(new Event('input'));
  });

  document.getElementById('btn-add-participant-edit').addEventListener('click', () => {
    const val = input.value.trim();
    if (val && !editParticipants.find(p => p.phone === val)) {
      editParticipants.push({ name: val, phone: val });
      renderEditParticipantTags();
      input.value = '';
    }
  });
}

initEditParticipantInput();

document.getElementById('edit-modal-backdrop').addEventListener('click', closeEditModal);
document.getElementById('edit-modal-close').addEventListener('click', closeEditModal);

document.getElementById('edit-modal-form').addEventListener('submit', async e => {
  e.preventDefault();
  if (!editingTrip) return;
  const data = {
    id: editingTrip.id,
    nombre: document.getElementById('edit-modal-name').value.trim(),
    destino: editingTrip.destino,
    fechaInicio: document.getElementById('edit-modal-start').value,
    fechaFin: document.getElementById('edit-modal-end').value,
    participantes: editParticipants.map(p => p.phone).join(', '),
    estado: editingTrip.estado,
    coverImage: editingTrip.coverImage || ''
  };
  try {
    const r = await fetch(`${API}/modificar-viaje`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
    const result = await r.json();
    if (result.ok) {
      closeEditModal();
      loadMyTrips();
      if (currentTripId === editingTrip.id) {
        openTripDetail(editingTrip.id);
      }
    }
  } catch {}
});

// ===== TRIP DETAIL =====
async function openTripDetail(tripId) {
  try {
    currentTripId = tripId;
    currentDay = 1;

    const trip = myTrips.find(t => t.id === tripId);
    if (!trip) return;

    document.getElementById('detail-dest').textContent = trip.destino || trip.nombre;
    document.getElementById('detail-date').textContent = formatDate(trip.fechaInicio, trip.fechaFin);

    const statusEl = document.getElementById('detail-status');
    statusEl.textContent = trip.estado || '';
    statusEl.style.color = trip.estado === 'Completado' ? 'var(--gray)' : 'var(--sky)';

    const cover = document.getElementById('detail-cover');
    if (trip.coverImage && trip.coverImage.startsWith('data:')) {
      cover.innerHTML = `<img src="${escapeHtml(trip.coverImage)}" alt=""><span id="cover-placeholder" class="cover-placeholder" style="display:none">Aún no hay imagen de portada</span><button id="btn-change-cover" class="detail-camera-btn">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg></button>
        <input type="file" accept="image/*" id="change-cover-file" style="display:none">`;
      document.getElementById('cover-placeholder').style.display = 'none';
    } else {
      cover.innerHTML = `<span id="cover-placeholder" class="cover-placeholder">Aún no hay imagen de portada</span><button id="btn-change-cover" class="detail-camera-btn">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg></button>
        <input type="file" accept="image/*" id="change-cover-file" style="display:none">`;
    }

    document.getElementById('btn-change-cover').addEventListener('click', () => {
      document.getElementById('change-cover-file').click();
    });
    document.getElementById('change-cover-file').addEventListener('change', e => {
      const file = e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = async ev => {
        const imgData = ev.target.result;
        const r = await fetch(`${API}/modificar-viaje`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id: tripId, coverImage: imgData }) });
        if ((await r.json()).ok) {
          openTripDetail(tripId);
          loadMyTrips();
        }
      };
      reader.readAsDataURL(file);
    });

    const completedBadge = document.getElementById('detail-completed-badge');
    const isCompleted = trip.estado === 'Completado';
    completedBadge.style.display = isCompleted ? 'block' : 'none';

    document.getElementById('btn-edit-trip-detail').style.display = isCompleted ? 'none' : 'block';
    document.getElementById('btn-change-cover').style.display = isCompleted ? 'none' : 'block';
    document.querySelector('.detail-bottom-actions').style.display = isCompleted ? 'none' : 'flex';
    document.getElementById('btn-show-add-activity').style.display = isCompleted ? 'none' : 'inline-block';

    const phones = (trip.participantes || '').split(',').map(s => s.trim()).filter(Boolean);
    const pnombres = trip.participantesNombres || {};
    const names = phones.map(p => pnombres[p] || p);
    if (trip.creadorNombre && !phones.includes(trip.creadorTelefono)) {
      names.unshift(trip.creadorNombre);
    }
    const participantsEl = document.getElementById('detail-participants');
    if (names.length > 0) {
      participantsEl.innerHTML = '<span style="font-size:0.8125rem;color:var(--gray);font-weight:500">Con:</span> ' +
        names.map(n => `<span class="participant-tag">${escapeHtml(n)}</span>`).join('');
      participantsEl.style.display = 'flex';
    } else {
      participantsEl.style.display = 'none';
    }

    const numDays = daysBetween(trip.fechaInicio, trip.fechaFin);
    initDayPills(numDays);

    await loadActivities();
    await loadAlbumFotos();

    document.getElementById('main-app').classList.add('hidden');
    document.getElementById('trip-detail').classList.add('active');
    saveAppState();
  } catch (e) {
    console.error('Error al abrir detalle del viaje:', e);
  }
}

document.getElementById('btn-back-detail').addEventListener('click', () => {
  document.getElementById('trip-detail').classList.remove('active');
  document.getElementById('main-app').classList.remove('hidden');
  currentTripId = null;
  currentDay = 1;
  currentActivities = [];
  saveAppState();
});

document.getElementById('btn-edit-trip-detail').addEventListener('click', () => {
  const trip = myTrips.find(t => t.id === currentTripId);
  if (trip && trip.estado !== 'Completado') openEditModal(trip);
});

// ===== DAY PILLS =====
function initDayPills(numDays) {
  const container = document.getElementById('day-pills');
  container.innerHTML = '';
  const trip = myTrips.find(t => t.id === currentTripId);
  if (!trip) return;
  const diasSemana = ['dom','lun','mar','mié','jue','vie','sáb'];
  const meses = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic'];
  for (let i = 1; i <= numDays; i++) {
    const d = new Date(trip.fechaInicio + 'T00:00:00');
    d.setDate(d.getDate() + i - 1);

    const dot = document.createElement('button');
    dot.className = 'timeline-dot';
    if (i === currentDay) dot.classList.add('active');
    else if (i < currentDay || trip.estado === 'Completado') dot.classList.add('completed');
    dot.innerHTML = `<span class="day-label">DÍA ${i}</span><span class="day-date">${d.getDate()} ${meses[d.getMonth()]} · ${diasSemana[d.getDay()]}</span>`;
    dot.addEventListener('click', () => goToDay(i));
    container.appendChild(dot);

    if (i < numDays) {
      const line = document.createElement('div');
      line.className = 'timeline-line';
      if (i < currentDay || trip.estado === 'Completado') line.classList.add('completed');
      container.appendChild(line);
    }
  }
  scrollToDay(currentDay);
  renderActivitiesForDay(currentDay);
}

function scrollToDay(day) {
  const pills = document.getElementById('day-pills');
  const dots = pills.querySelectorAll('.timeline-dot');
  if (dots[day - 1]) dots[day - 1].scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' });
}

function goToDay(day) {
  currentDay = day;
  const pills = document.getElementById('day-pills');
  pills.querySelectorAll('.timeline-dot').forEach((dot, i) => {
    dot.classList.toggle('active', i + 1 === day);
    dot.classList.toggle('completed', i + 1 < day);
  });
  pills.querySelectorAll('.timeline-line').forEach((line, i) => {
    line.classList.toggle('completed', i + 1 < day);
  });
  scrollToDay(day);
  renderActivitiesForDay(day);
}

document.getElementById('day-prev').addEventListener('click', () => {
  const numDays = document.querySelectorAll('.timeline-dot').length;
  if (currentDay > 1) goToDay(currentDay - 1);
});

document.getElementById('day-next').addEventListener('click', () => {
  const numDays = document.querySelectorAll('.timeline-dot').length;
  if (currentDay < numDays) goToDay(currentDay + 1);
});


// ===== ACTIVITIES =====
async function loadActivities() {
  if (!currentTripId) return;
  try {
    const r = await fetch(`${API}/actividades?viajeId=${currentTripId}`);
    const data = await r.json();
    if (data.ok) {
      currentActivities = data.actividades || [];
      renderActivitiesForDay(currentDay);
    }
  } catch {}
}

function renderActivitiesForDay(day) {
  const list = document.getElementById('activities-list');
  const dayActs = currentActivities.filter(a => a.dia === day);
  if (dayActs.length === 0) {
    list.innerHTML = '<div class="acts-empty"><svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg><p class="acts-empty-title">No hay actividades planificadas</p><p class="acts-empty-sub">Añade tu primera actividad</p></div>';
    return;
  }
  list.innerHTML = dayActs.sort((a, b) => (a.hora || '').localeCompare(b.hora || '')).map(a => {
    const svgMap = {
      'hotel': '<path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" fill="none" stroke="#EEB19A" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><polyline points="9 22 9 12 15 12 15 22" fill="none" stroke="#EEB19A" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
      'transporte': '<path d="M21 16v-2l-8-5V3.5A1.5 1.5 0 0 0 11.5 2 1.5 1.5 0 0 0 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z" fill="none" stroke="#EEB19A" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
      'vuelo': '<path d="M21 16v-2l-8-5V3.5A1.5 1.5 0 0 0 11.5 2 1.5 1.5 0 0 0 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z" fill="none" stroke="#EEB19A" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>',
      'actividad': '<circle cx="12" cy="12" r="10" fill="none" stroke="#EEB19A" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><polyline points="12 6 12 12 16 14" fill="none" stroke="#EEB19A" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>'
    };
    const paths = svgMap[a.tipo] || svgMap['actividad'];
    const timeStr = a.horaFin ? `${escapeHtml(a.hora)} – ${escapeHtml(a.horaFin)}` : (a.hora ? escapeHtml(a.hora) : '');
    return `<div class="activity-card">
      <div class="activity-card-icon-block">
        <svg width="20" height="20" viewBox="0 0 24 24">${paths}</svg>
      </div>
      <div class="activity-card-info">
        <div class="activity-card-title">${escapeHtml(a.nombre)}</div>
        ${a.descripcion ? `<div class="activity-card-desc">${escapeHtml(a.descripcion)}</div>` : ''}
        ${timeStr ? `<div class="activity-card-time"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>${timeStr}</div>` : ''}
      </div>
      <button class="activity-card-delete" data-id="${a.id}"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#EEB19A" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
    </div>`;
  }).join('');
  list.querySelectorAll('.activity-card-delete').forEach(btn => {
    btn.addEventListener('click', () => deleteActivity(parseInt(btn.dataset.id)));
  });
}

document.getElementById('btn-show-add-activity').addEventListener('click', () => {
  const form = document.getElementById('activity-form');
  form.style.display = form.style.display === 'none' ? 'block' : 'none';
});

document.getElementById('activity-form').addEventListener('submit', async e => {
  e.preventDefault();
  const data = {
    viajeId: currentTripId,
    dia: currentDay,
    hora: document.getElementById('act-hora').value || '',
    horaFin: document.getElementById('act-fin').value || '',
    tipo: document.getElementById('act-tipo').value || 'actividad',
    nombre: document.getElementById('act-nombre').value.trim(),
    descripcion: document.getElementById('act-desc').value.trim() || ''
  };
  if (!data.nombre) return;
  try {
    const r = await fetch(`${API}/actividades`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
    const result = await r.json();
    if (result.ok) {
      document.getElementById('activity-form').reset();
      document.getElementById('activity-form').style.display = 'none';
      await loadActivities();
    }
  } catch {}
});

async function deleteActivity(id) {
  try {
    await fetch(`${API}/actividades`, { method: 'DELETE', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id }) });
    await loadActivities();
  } catch {}
}

// ===== BOTTOM ACTIONS =====
document.getElementById('btn-edit-trip').addEventListener('click', async () => {
  const trip = myTrips.find(t => t.id === currentTripId);
  if (!trip) return;
  if (trip.estado === 'Completado') return;
  const nuevoEstado = trip.estado === 'En preparación' ? 'En curso' : 'Completado';
  try {
    const r = await fetch(`${API}/modificar-viaje`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...trip, estado: nuevoEstado, participantes: trip.participantes || '', coverImage: trip.coverImage || '' }) });
    if ((await r.json()).ok) {
      await loadMyTrips();
      document.getElementById('trip-detail').classList.remove('active');
      document.getElementById('main-app').classList.remove('hidden');
      currentTripId = null;
      currentDay = 1;
      currentActivities = [];
      saveAppState();
    }
  } catch {}
});

document.getElementById('btn-delete-trip').addEventListener('click', async () => {
  if (!confirm('¿Eliminar este viaje permanentemente?')) return;
  await deleteTrip(currentTripId);
});

// ===== SETTINGS =====
document.getElementById('btn-settings').addEventListener('click', () => {
  document.getElementById('main-app').classList.add('hidden');
  document.getElementById('settings-screen').classList.add('active');
  loadInvitacionesBadge();
  saveAppState();
});

document.getElementById('btn-back-settings').addEventListener('click', () => {
  document.getElementById('settings-screen').classList.remove('active');
  document.getElementById('main-app').classList.remove('hidden');
  saveAppState();
});

// ===== PROFILE =====
document.getElementById('btn-edit-profile').addEventListener('click', async () => {
  document.getElementById('settings-screen').classList.remove('active');
  document.getElementById('edit-profile-screen').classList.add('active');
  hideError(document.getElementById('edit-profile-error'));
  hideSuccess(document.getElementById('edit-profile-success'));

  try {
    const r = await fetch(`${API}/perfil`);
    const data = await r.json();
    if (data.ok) {
      document.getElementById('edit-name').value = data.perfil.nombre || '';
      document.getElementById('edit-phone').value = data.perfil.telefono || '';
      document.getElementById('edit-email').value = data.perfil.email || '';
      const foto = data.perfil.fotoPerfil || '';
      document.getElementById('edit-photo').value = foto;
      updateProfileAvatar(foto);
    }
  } catch {}
});

function updateProfileAvatar(src) {
  const el = document.getElementById('profile-avatar');
  if (src && src.startsWith('data:')) {
    el.innerHTML = `<img src="${escapeHtml(src)}" style="width:100%;height:100%;object-fit:cover;border-radius:50%">`;
  } else {
    el.innerHTML = `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#92C0E8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`;
  }
}

document.getElementById('btn-back-profile').addEventListener('click', () => {
  document.getElementById('edit-profile-screen').classList.remove('active');
  document.getElementById('settings-screen').classList.add('active');
});

document.getElementById('btn-select-photo').addEventListener('click', () => {
  document.getElementById('edit-photo-file').click();
});

document.getElementById('edit-photo-file').addEventListener('change', e => {
  const file = e.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = ev => {
    const result = ev.target.result;
    document.getElementById('edit-photo').value = result;
    updateProfileAvatar(result);
  };
  reader.readAsDataURL(file);
});

document.getElementById('edit-profile-form').addEventListener('submit', async e => {
  e.preventDefault();
  const errEl = document.getElementById('edit-profile-error');
  const successEl = document.getElementById('edit-profile-success');
  hideError(errEl);
  hideSuccess(successEl);

  const data = {
    nombre: document.getElementById('edit-name').value.trim(),
    email: document.getElementById('edit-email').value.trim(),
    fotoPerfil: document.getElementById('edit-photo').value || null
  };

  try {
    const r = await fetch(`${API}/perfil`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
    const result = await r.json();
    if (result.ok) {
      showSuccess(successEl, 'Perfil actualizado correctamente');
    } else {
      showError(errEl, result.error || 'Error al actualizar');
    }
  } catch {
    showError(errEl, 'Error de conexión');
  }
});

// ===== CONTACTOS =====
async function loadContactosFromServer() {
  try {
    const r = await fetch(`${API}/contactos`);
    const data = await r.json();
    if (data.ok) contactos = data.contactos || [];
  } catch {}
}

document.getElementById('btn-contactos').addEventListener('click', () => {
  document.getElementById('settings-screen').classList.remove('active');
  document.getElementById('contactos-screen').classList.add('active');
  renderContactos();
});

document.getElementById('btn-back-contactos').addEventListener('click', () => {
  document.getElementById('contactos-screen').classList.remove('active');
  document.getElementById('settings-screen').classList.add('active');
});

function renderContactos() {
  const list = document.getElementById('contactos-list');
  const empty = document.getElementById('contactos-empty');
  const errEl = document.getElementById('contacto-error');
  hideError(errEl);

  if (contactos.length === 0) {
    list.innerHTML = '';
    empty.style.display = 'flex';
    return;
  }

  empty.style.display = 'none';
  list.innerHTML = contactos.map(c => `
    <div class="contacto-item">
      <div class="contacto-avatar">${escapeHtml(c.nombre.charAt(0).toUpperCase())}</div>
      <div class="contacto-info">
        <div class="contacto-name">${escapeHtml(c.nombre)}</div>
        <div class="contacto-phone">${escapeHtml(c.telefono)}</div>
      </div>
      <button class="contacto-delete" data-phone="${escapeHtml(c.telefono)}">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
      </button>
    </div>
  `).join('');

  list.querySelectorAll('.contacto-delete').forEach(btn => {
    btn.addEventListener('click', async () => {
      const errEl = document.getElementById('contacto-error');
      hideError(errEl);
      try {
        const r = await fetch(`${API}/contactos?telefono=${encodeURIComponent(btn.dataset.phone)}`, { method: 'DELETE' });
        const data = await r.json();
        if (data.ok) {
          contactos = contactos.filter(c => c.telefono !== btn.dataset.phone);
          renderContactos();
        } else {
          showError(errEl, data.error || 'Error al eliminar');
        }
      } catch {
        showError(errEl, 'Error de conexión');
      }
    });
  });
}

document.getElementById('add-contacto-form').addEventListener('submit', async e => {
  e.preventDefault();
  const errEl = document.getElementById('contacto-error');
  hideError(errEl);

  const phone = document.getElementById('contacto-telefono').value.trim();

  if (!phone) {
    showError(errEl, 'Introduce un teléfono');
    return;
  }

  if (contactos.find(c => c.telefono === phone)) {
    showError(errEl, 'Este contacto ya existe');
    return;
  }

  try {
    const existeRes = await fetch(`${API}/usuarios/existe?telefono=${encodeURIComponent(phone)}`);
    const existeData = await existeRes.json();
    if (!existeData.ok || !existeData.existe) {
      showError(errEl, 'El teléfono no pertenece a un usuario registrado');
      return;
    }
  } catch {
    showError(errEl, 'Error de conexión');
    return;
  }

  try {
    const r = await fetch(`${API}/contactos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ telefono: phone })
    });
    const data = await r.json();
    if (data.ok) {
      contactos.push({ nombre: data.nombre, telefono: phone });
      document.getElementById('add-contacto-form').reset();
      renderContactos();
    } else {
      showError(errEl, data.error || 'Error al añadir contacto');
    }
  } catch {
    showError(errEl, 'Error de conexión');
  }
});

// ===== INVITACIONES =====
let invitacionesData = [];

document.getElementById('btn-invitaciones').addEventListener('click', () => {
  document.getElementById('settings-screen').classList.remove('active');
  document.getElementById('invitaciones-screen').classList.add('active');
  loadInvitaciones();
});

document.getElementById('btn-back-invitaciones').addEventListener('click', () => {
  document.getElementById('invitaciones-screen').classList.remove('active');
  document.getElementById('settings-screen').classList.add('active');
});

async function loadInvitacionesBadge() {
  const badge = document.getElementById('invitaciones-badge');
  const settingsBadge = document.getElementById('settings-badge');
  try {
    const r = await fetch(`${API}/invitaciones`);
    const data = await r.json();
    if (data.ok && data.invitaciones) {
      const pendientes = data.invitaciones.filter(i => i.status === 'pendiente');
      const count = pendientes.length;
      if (count > 0) {
        badge.textContent = count;
        badge.style.display = 'inline';
        settingsBadge.textContent = count;
        settingsBadge.style.display = 'inline';
      } else {
        badge.style.display = 'none';
        settingsBadge.style.display = 'none';
      }
    }
  } catch {}
}

async function loadInvitaciones() {
  const loading = document.getElementById('invitaciones-loading');
  const list = document.getElementById('invitaciones-list');
  const empty = document.getElementById('invitaciones-empty');
  loading.style.display = 'block';
  list.innerHTML = '';
  empty.style.display = 'none';

  try {
    const r = await fetch(`${API}/invitaciones`);
    const data = await r.json();
    loading.style.display = 'none';

    if (data.ok && data.invitaciones && data.invitaciones.length > 0) {
      invitacionesData = data.invitaciones;
      list.innerHTML = data.invitaciones.map(inv => {
        const hasCover = inv.image && inv.image.startsWith('data:');
        const imgHtml = hasCover
          ? `<img class="invitation-card-img" src="${escapeHtml(inv.image)}" alt="">`
          : `<div class="invitation-card-placeholder">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="m21 15-5-5L5 21"/></svg>
             </div>`;
        let statusHtml;
        if (inv.status === 'pendiente') {
          statusHtml = `
            <div class="invitation-card-actions">
              <button class="invitation-card-btn invitation-card-btn-accept inv-accept" data-viajeid="${inv.viajeId}">✓ Aceptar</button>
              <button class="invitation-card-btn invitation-card-btn-reject inv-reject" data-viajeid="${inv.viajeId}">✕ Rechazar</button>
            </div>`;
        } else {
          const isAccepted = inv.status === 'aceptada';
          statusHtml = `<span class="invitation-card-status ${isAccepted ? 'accepted' : 'rejected'}">${isAccepted ? '✓ Aceptada' : '✕ Rechazada'}</span>`;
        }
        return `
          <div class="invitation-card">
            ${imgHtml}
            <div class="invitation-card-body">
              <h3 class="invitation-card-title">${escapeHtml(inv.tripName || 'Invitación a viaje')}</h3>
              <div class="invitation-card-info">
                <div class="invitation-card-info-row">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  <span>${escapeHtml(inv.dates || '')}</span>
                </div>
                <div class="invitation-card-info-row">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                  <span>Invitado por: ${escapeHtml(inv.invitedBy || 'Desconocido')}</span>
                </div>
              </div>
              ${statusHtml}
            </div>
          </div>`;
      }).join('');

      list.querySelectorAll('.inv-accept').forEach(btn => {
        btn.addEventListener('click', () => handleInvitacion(parseInt(btn.dataset.viajeid), true));
      });
      list.querySelectorAll('.inv-reject').forEach(btn => {
        btn.addEventListener('click', () => handleInvitacion(parseInt(btn.dataset.viajeid), false));
      });
    } else {
      empty.style.display = 'flex';
    }
  } catch {
    loading.style.display = 'none';
    empty.style.display = 'flex';
  }
}

async function handleInvitacion(viajeId, accept) {
  try {
    const r = await fetch(`${API}/invitaciones`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ viajeId, aceptar: accept }) });
    const data = await r.json();
    if (data.ok) {
      loadInvitaciones();
      loadInvitacionesBadge();
      if (accept) {
        loadMyTrips();
        document.querySelector('.tab-item[data-tab="current-trips"]').click();
      }
    }
  } catch {}
}

// ===== HELP ACCORDION =====
document.getElementById('btn-back-help').addEventListener('click', () => {
  document.getElementById('help-screen').classList.remove('active');
  document.getElementById('settings-screen').classList.add('active');
});

document.getElementById('btn-help').addEventListener('click', () => {
  document.getElementById('settings-screen').classList.remove('active');
  document.getElementById('help-screen').classList.add('active');
});

document.querySelectorAll('.help-question').forEach(q => {
  q.addEventListener('click', () => {
    q.classList.toggle('open');
    const answer = q.nextElementSibling;
    answer.classList.toggle('open');
  });
});

// ===== SUPPORT =====
document.getElementById('btn-back-support').addEventListener('click', () => {
  document.getElementById('support-screen').classList.remove('active');
  document.getElementById('settings-screen').classList.add('active');
});

document.getElementById('btn-support').addEventListener('click', () => {
  document.getElementById('settings-screen').classList.remove('active');
  document.getElementById('support-screen').classList.add('active');
});

document.getElementById('support-form').addEventListener('submit', e => {
  e.preventDefault();
  const errEl = document.getElementById('support-error');
  const successEl = document.getElementById('support-success');
  hideError(errEl);
  hideSuccess(successEl);

  const name = document.getElementById('support-name').value.trim();
  const email = document.getElementById('support-email').value.trim();
  const subject = document.getElementById('support-subject').value.trim();
  const message = document.getElementById('support-message').value.trim();

  if (!name || !email || !subject || !message) {
    showError(errEl, 'Completa todos los campos');
    return;
  }

  showSuccess(successEl, 'Mensaje enviado correctamente. Te responderemos pronto.');
  document.getElementById('support-form').reset();
});

// ===== ADMIN EASTER EGG =====
document.getElementById('login-logo').addEventListener('click', () => {
  adminClickCount++;
  if (adminClickCount >= 5) {
    adminClickCount = 0;
    document.getElementById('login-screen').classList.remove('active');
    document.getElementById('admin-login-screen').classList.add('active');
    hideError(document.getElementById('admin-login-error'));
  }
});

document.getElementById('go-back-from-admin-login').addEventListener('click', () => {
  document.getElementById('admin-login-screen').classList.remove('active');
  document.getElementById('login-screen').classList.add('active');
  hideError(document.getElementById('admin-login-error'));
});

document.getElementById('admin-login-form').addEventListener('submit', async e => {
  e.preventDefault();
  const password = document.getElementById('admin-password').value.trim();
  const errEl = document.getElementById('admin-login-error');
  hideError(errEl);

  if (password !== '4225') {
    showError(errEl, 'Contraseña incorrecta');
    return;
  }

  try {
    const r1 = await fetch(`${API}/login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ telefono: '618941168' }) });
    const d1 = await r1.json();
    if (!d1.ok) { showError(errEl, d1.error || 'Error de acceso'); return; }

    const r2 = await fetch(`${API}/verify-login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ telefono: '618941168', codigo: '123456' }) });
    const d2 = await r2.json();
    if (!d2.ok) { showError(errEl, d2.error || 'Error de verificación'); return; }

    const r3 = await fetch(`${API}/perfil`);
    const d3 = await r3.json();
    if (d3.ok) {
      currentUser = d3.perfil;
      document.getElementById('settings-admin-group').style.display = 'block';
    }

    document.getElementById('admin-login-screen').classList.remove('active');
    document.getElementById('admin-panel-screen').classList.add('active');
    document.getElementById('admin-password').value = '';
    adminPanelOrigin = 'login';
    loadAdminData();
  } catch {
    showError(errEl, 'Error de conexión');
  }
});

document.getElementById('btn-back-from-admin').addEventListener('click', () => {
  document.getElementById('admin-panel-screen').classList.remove('active');
  if (adminPanelOrigin === 'login') {
    showMainApp();
  } else {
    document.getElementById('settings-screen').classList.add('active');
  }
  adminPanelOrigin = null;
});

document.getElementById('btn-admin-panel').addEventListener('click', () => {
  document.getElementById('settings-screen').classList.remove('active');
  document.getElementById('admin-panel-screen').classList.add('active');
  adminPanelOrigin = 'settings';
  loadAdminData();
});

// ===== ADMIN DATA =====
let adminFilter = 'all';
let adminUsers = [];
let adminTrips = [];

async function loadAdminData() {
  try {
    const [statsRes, usersRes, tripsRes] = await Promise.all([
      fetch(`${API}/admin/stats`).then(r => r.json()),
      fetch(`${API}/admin/usuarios`).then(r => r.json()),
      fetch(`${API}/admin/viajes`).then(r => r.json())
    ]);

    if (statsRes.ok) {
      document.getElementById('admin-stat-all').textContent = (statsRes.totalUsuarios || 0) + (statsRes.totalViajes || 0);
      document.getElementById('admin-stat-usuarios').textContent = statsRes.totalUsuarios || 0;
      document.getElementById('admin-stat-viajes').textContent = statsRes.totalViajes || 0;
      document.getElementById('admin-stat-activos').textContent = statsRes.viajesActivos || 0;
      document.getElementById('admin-stat-completados').textContent = statsRes.viajesCompletados || 0;
    }

    if (usersRes.ok) adminUsers = usersRes.usuarios || [];
    if (tripsRes.ok) adminTrips = tripsRes.viajes || [];

    applyAdminFilter(adminFilter);
  } catch {}
}

function applyAdminFilter(filter) {
  adminFilter = filter;
  document.querySelectorAll('.admin-stat-card').forEach(c => c.classList.toggle('active', c.dataset.filter === filter));

  const usersSection = document.getElementById('admin-users-section');
  const tripsSection = document.getElementById('admin-trips-section');
  const tripsList = document.getElementById('admin-trips-list');

  usersSection.style.display = 'none';
  tripsSection.style.display = 'none';

  if (filter === 'all' || filter === 'usuarios') {
    if (filter === 'all') tripsSection.style.display = 'block';
    usersSection.style.display = 'block';
    const usersList = document.getElementById('admin-users-list');
    usersList.innerHTML = adminUsers.map(u => `
      <div class="admin-user-item">
        <div class="admin-user-info">
          <div class="admin-user-name">${escapeHtml(u.nombre || 'Sin nombre')} ${u.esAdmin ? '<span class="admin-badge">Admin</span>' : ''}</div>
          <div class="admin-user-detail">${escapeHtml(u.telefono)} ${u.email ? '· '+escapeHtml(u.email) : ''}</div>
        </div>
        <button class="admin-delete-btn" data-id="${u.id}" title="Eliminar usuario">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
        </button>
      </div>
    `).join('');
    usersList.querySelectorAll('.admin-delete-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        if (confirm('¿Eliminar este usuario?')) {
          await fetch(`${API}/admin/usuarios`, { method: 'DELETE', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id: parseInt(btn.dataset.id) }) });
          loadAdminData();
        }
      });
    });
  }

  if (filter === 'all' || filter === 'viajes' || filter === 'activos' || filter === 'completados') {
    tripsSection.style.display = 'block';
    let filtered = adminTrips;
    if (filter === 'activos') filtered = adminTrips.filter(t => t.estado === 'En preparación' || t.estado === 'En curso');
    else if (filter === 'completados') filtered = adminTrips.filter(t => t.estado === 'Completado');
    tripsList.innerHTML = filtered.map(t => `
      <div class="admin-trip-item">
        <div class="admin-trip-info">
          <div class="admin-user-name">${escapeHtml(t.nombre)}</div>
          <div class="admin-user-detail">${escapeHtml(t.destino)} · ${escapeHtml(t.estado)}</div>
        </div>
        <button class="admin-delete-btn" data-id="${t.id}" title="Eliminar viaje">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
        </button>
      </div>
    `).join('');
    tripsList.querySelectorAll('.admin-delete-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        if (confirm('¿Eliminar este viaje?')) {
          await fetch(`${API}/admin/viajes`, { method: 'DELETE', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id: parseInt(btn.dataset.id) }) });
          loadAdminData();
        }
      });
    });
  }
}

document.querySelectorAll('.admin-stat-card').forEach(card => {
  card.addEventListener('click', () => {
    const filter = card.dataset.filter;
    applyAdminFilter(filter === adminFilter ? 'all' : filter);
  });
});

// ===== ALBUM FOTOS =====
async function loadAlbumFotos() {
  try {
    const r = await fetch(`${API}/album-fotos?viajeId=${currentTripId}`);
    const res = await r.json();
    if (res.ok) {
      renderAlbumFotos(res.fotos || []);
    }
  } catch {}
}

function renderAlbumFotos(fotos) {
  const list = document.getElementById('album-list');
  if (!fotos || fotos.length === 0) {
    list.innerHTML = '<div class="album-empty"><svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg><p class="album-empty-text">No hay fotos</p></div>';
    return;
  }
  list.innerHTML = '<div class="album-grid">' + fotos.map(f =>
    `<div class="album-photo"><img src="${escapeHtml(f.imagen)}" alt=""></div>`
  ).join('') + '</div>';
}

document.getElementById('btn-upload-album-photo').addEventListener('click', () => {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  input.onchange = async e => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = async ev => {
      try {
        const r = await fetch(`${API}/album-fotos`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ viajeId: currentTripId, imagen: ev.target.result }) });
        const res = await r.json();
        if (res.ok) {
          await loadAlbumFotos();
        }
      } catch {}
    };
    reader.readAsDataURL(file);
  };
  input.click();
});

// ===== INIT =====
document.addEventListener('DOMContentLoaded', () => {
  checkSession();
});
