/**
 * BloodLink - Frontend Controller & API Integration
 */

const API_BASE = '/api';

// State store
let state = {
    bloodGroups: [],
    donors: [],
    donations: [],
    searchResults: []
};

// DOM Elements
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initDateInputs();
    initForms();
    initModal();
    loadAllData();
});

// Toast notification helper
function showToast(title, message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    toast.innerHTML = `
        <div class="toast-content">
            <div class="toast-title">${escapeHtml(title)}</div>
            <div class="toast-msg">${escapeHtml(message)}</div>
        </div>
        <button class="toast-close" onclick="this.parentElement.remove()">&times;</button>
    `;
    
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(50px)';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}

// Escape HTML utility
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.innerText = text;
    return div.innerHTML;
}

// Date helpers
function initDateInputs() {
    const today = new Date().toISOString().split('T')[0];
    const donationDateInput = document.getElementById('donation-date');
    const modalDonationDateInput = document.getElementById('modal-donation-date');
    
    if (donationDateInput) {
        donationDateInput.value = today;
        donationDateInput.max = today;
    }
    if (modalDonationDateInput) {
        modalDonationDateInput.value = today;
        modalDonationDateInput.max = today;
    }
}

function formatDate(dateStr) {
    if (!dateStr) return '<span class="text-muted">Never donated</span>';
    try {
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    } catch {
        return dateStr;
    }
}

// Navigation Tabs
function initNavigation() {
    const navButtons = document.querySelectorAll('.nav-btn');
    const tabPanels = document.querySelectorAll('.tab-panel');

    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.getAttribute('data-tab');
            
            navButtons.forEach(b => b.classList.remove('active'));
            tabPanels.forEach(p => p.classList.remove('active'));

            btn.classList.add('active');
            const panel = document.getElementById(targetTab);
            if (panel) panel.classList.add('active');
        });
    });

    // Refresh buttons
    document.getElementById('btn-refresh-history')?.addEventListener('click', loadDonationHistory);
    document.getElementById('btn-refresh-donors')?.addEventListener('click', loadDonors);
}

// Form Handlers
function initForms() {
    // Search Donors Form
    const searchForm = document.getElementById('search-donor-form');
    if (searchForm) {
        searchForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const bloodGroupId = document.getElementById('search-blood-group').value;
            const city = document.getElementById('search-city').value.trim();

            if (!bloodGroupId || !city) {
                showToast('Validation Error', 'Please select a blood group and enter a city.', 'error');
                return;
            }

            await searchDonors(bloodGroupId, city);
        });
    }

    // Reset Search
    document.getElementById('btn-reset-search')?.addEventListener('click', () => {
        searchForm.reset();
        document.getElementById('search-count-badge').innerText = '0 Found';
        document.getElementById('search-results-tbody').innerHTML = `
            <tr class="empty-row"><td colspan="7">Select blood group and city, then click Search Donors.</td></tr>
        `;
    });

    // Register Donor Form
    const registerForm = document.getElementById('register-donor-form');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('donor-name').value.trim();
            const phone = document.getElementById('donor-phone').value.trim();
            const city = document.getElementById('donor-city').value.trim();
            const bloodGroupId = document.getElementById('donor-blood-group').value;

            if (!name || !phone || !city || !bloodGroupId) {
                showToast('Validation Error', 'All fields are required.', 'error');
                return;
            }

            const donorPayload = {
                name: name,
                phone: phone,
                city: city,
                bloodGroup: {
                    id: parseInt(bloodGroupId, 10)
                }
            };

            await registerDonor(donorPayload);
        });
    }

    // Record Donation Form
    const donationForm = document.getElementById('record-donation-form');
    if (donationForm) {
        donationForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const donorId = document.getElementById('donation-donor-select').value;
            const donationDate = document.getElementById('donation-date').value;

            if (!donorId || !donationDate) {
                showToast('Validation Error', 'Please select a donor and donation date.', 'error');
                return;
            }

            await submitDonation(parseInt(donorId, 10), donationDate);
        });
    }

    // Add Blood Group Form
    const bgForm = document.getElementById('add-blood-group-form');
    if (bgForm) {
        bgForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const groupNameInput = document.getElementById('new-blood-group-name');
            const groupName = groupNameInput.value.trim().toUpperCase();

            if (!groupName) {
                showToast('Validation Error', 'Blood group name cannot be empty.', 'error');
                return;
            }

            await addBloodGroup(groupName);
            groupNameInput.value = '';
        });
    }
}

// Modal Handlers
function initModal() {
    const modal = document.getElementById('quick-donate-modal');
    const closeBtn = document.getElementById('modal-close-btn');
    const cancelBtn = document.getElementById('modal-cancel-btn');
    const quickForm = document.getElementById('quick-donation-form');

    const closeModal = () => {
        modal.classList.add('hidden');
    };

    closeBtn?.addEventListener('click', closeModal);
    cancelBtn?.addEventListener('click', closeModal);

    modal?.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });

    quickForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const donorId = document.getElementById('modal-donor-id').value;
        const donationDate = document.getElementById('modal-donation-date').value;

        if (!donorId || !donationDate) {
            showToast('Validation Error', 'Please enter a valid donation date.', 'error');
            return;
        }

        const success = await submitDonation(parseInt(donorId, 10), donationDate);
        if (success) {
            closeModal();
            // Re-run current search if present
            const currentBg = document.getElementById('search-blood-group').value;
            const currentCity = document.getElementById('search-city').value.trim();
            if (currentBg && currentCity) {
                searchDonors(currentBg, currentCity);
            }
        }
    });
}

function openQuickDonateModal(donorId, donorName, bloodGroup, city) {
    const modal = document.getElementById('quick-donate-modal');
    document.getElementById('modal-donor-id').value = donorId;
    document.getElementById('modal-donor-name').innerText = donorName;
    document.getElementById('modal-donor-details').innerHTML = `
        <strong>${escapeHtml(donorName)}</strong> &bull; 
        Blood Group: <span class="blood-badge">${escapeHtml(bloodGroup)}</span> &bull; 
        City: <strong>${escapeHtml(city)}</strong>
    `;
    
    const today = new Date().toISOString().split('T')[0];
    const dateInput = document.getElementById('modal-donation-date');
    dateInput.value = today;
    dateInput.max = today;

    modal.classList.remove('hidden');
}

// ================= API CALLS ================= //

// Load All Initial Data
async function loadAllData() {
    await Promise.all([
        loadBloodGroups(),
        loadDonors(),
        loadDonationHistory()
    ]);
}

// 1. Blood Groups API
async function loadBloodGroups() {
    try {
        const res = await fetch(`${API_BASE}/blood-groups`);
        if (!res.ok) throw new Error('Failed to fetch blood groups');
        
        state.bloodGroups = await res.json();
        renderBloodGroupDropdowns();
        renderBloodGroupCards();
        updateStats();
    } catch (err) {
        showToast('Error', err.message, 'error');
    }
}

async function addBloodGroup(groupName) {
    try {
        const res = await fetch(`${API_BASE}/blood-groups`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ groupName: groupName })
        });

        const data = await res.json();

        if (res.status === 201 || res.status === 200) {
            showToast('Success', `Blood group "${data.groupName}" added successfully!`, 'success');
            await loadBloodGroups();
        } else if (res.status === 409) {
            showToast('Conflict', data.message || `Blood group "${groupName}" already exists.`, 'error');
        } else {
            showToast('Error', data.message || 'Failed to add blood group.', 'error');
        }
    } catch (err) {
        showToast('Network Error', err.message, 'error');
    }
}

// 2. Donors API
async function loadDonors() {
    try {
        const res = await fetch(`${API_BASE}/donors`);
        if (!res.ok) throw new Error('Failed to fetch donors');

        state.donors = await res.json();
        renderAllDonorsTable();
        renderDonationDonorDropdown();
        updateStats();
    } catch (err) {
        showToast('Error', err.message, 'error');
    }
}

async function registerDonor(donorPayload) {
    try {
        const res = await fetch(`${API_BASE}/donors`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(donorPayload)
        });

        const data = await res.json();

        if (res.status === 201 || res.status === 200) {
            showToast('Donor Registered', `Donor ${data.name} was successfully registered!`, 'success');
            document.getElementById('register-donor-form').reset();
            await loadDonors();
        } else {
            const errorMsg = data.validationErrors ? Object.values(data.validationErrors).join(', ') : data.message;
            showToast('Registration Failed', errorMsg || 'Unable to register donor.', 'error');
        }
    } catch (err) {
        showToast('Network Error', err.message, 'error');
    }
}

async function searchDonors(bloodGroupId, city) {
    const tbody = document.getElementById('search-results-tbody');
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2rem;">Searching for eligible donors...</td></tr>`;
    
    try {
        const url = `${API_BASE}/donors/search?bloodGroupId=${encodeURIComponent(bloodGroupId)}&city=${encodeURIComponent(city)}`;
        const res = await fetch(url);
        
        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            throw new Error(errData.message || 'Search request failed.');
        }

        const donors = await res.json();
        state.searchResults = donors;
        renderSearchResults(donors);
    } catch (err) {
        showToast('Search Failed', err.message, 'error');
        tbody.innerHTML = `<tr class="empty-row"><td colspan="7">Search failed: ${escapeHtml(err.message)}</td></tr>`;
    }
}

// 3. Donation API
async function submitDonation(donorId, donationDate) {
    try {
        const payload = {
            donor: { id: donorId },
            donationDate: donationDate
        };

        const res = await fetch(`${API_BASE}/donations`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (res.status === 201 || res.status === 200) {
            showToast('Donation Recorded', `Blood donation logged for ${data.donor ? data.donor.name : 'donor'}!`, 'success');
            document.getElementById('record-donation-form')?.reset();
            initDateInputs();
            await Promise.all([loadDonors(), loadDonationHistory()]);
            return true;
        } else {
            showToast('Failed to Record', data.message || 'Unable to record donation.', 'error');
            return false;
        }
    } catch (err) {
        showToast('Network Error', err.message, 'error');
        return false;
    }
}

async function loadDonationHistory() {
    try {
        const res = await fetch(`${API_BASE}/donations`);
        if (!res.ok) throw new Error('Failed to fetch donation records');

        state.donations = await res.json();
        renderDonationHistoryTable();
        updateStats();
    } catch (err) {
        showToast('Error', err.message, 'error');
    }
}

// ================= RENDER FUNCTIONS ================= //

function renderBloodGroupDropdowns() {
    const searchSelect = document.getElementById('search-blood-group');
    const registerSelect = document.getElementById('donor-blood-group');

    const createOptions = (groups) => {
        let html = '<option value="">-- Select Blood Group --</option>';
        groups.forEach(bg => {
            html += `<option value="${bg.id}">${escapeHtml(bg.groupName)}</option>`;
        });
        return html;
    };

    if (searchSelect) searchSelect.innerHTML = createOptions(state.bloodGroups);
    if (registerSelect) registerSelect.innerHTML = createOptions(state.bloodGroups);
}

function renderBloodGroupCards() {
    const container = document.getElementById('blood-groups-container');
    if (!container) return;

    if (state.bloodGroups.length === 0) {
        container.innerHTML = '<p class="text-muted">No blood groups found.</p>';
        return;
    }

    container.innerHTML = state.bloodGroups.map(bg => `
        <div class="blood-group-chip">
            <span class="bg-name">${escapeHtml(bg.groupName)}</span>
            <span class="bg-id">ID: #${bg.id}</span>
        </div>
    `).join('');
}

function renderSearchResults(donors) {
    const tbody = document.getElementById('search-results-tbody');
    const badge = document.getElementById('search-count-badge');
    
    badge.innerText = `${donors.length} Found`;
    badge.className = donors.length > 0 ? 'badge badge-success' : 'badge badge-info';

    if (donors.length === 0) {
        tbody.innerHTML = `
            <tr class="empty-row">
                <td colspan="7">No eligible donors found matching the blood group and city criteria.</td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = donors.map(donor => `
        <tr>
            <td><strong>${escapeHtml(donor.name)}</strong></td>
            <td><a href="tel:${escapeHtml(donor.phone)}" style="color:var(--primary); font-weight:600; text-decoration:none;">${escapeHtml(donor.phone)}</a></td>
            <td>${escapeHtml(donor.city)}</td>
            <td><span class="blood-badge">${escapeHtml(donor.bloodGroup ? donor.bloodGroup.groupName : '-')}</span></td>
            <td><span class="badge badge-success">Available</span></td>
            <td>${formatDate(donor.lastDonationDate)}</td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="openQuickDonateModal(${donor.id}, '${escapeHtml(donor.name)}', '${escapeHtml(donor.bloodGroup ? donor.bloodGroup.groupName : '')}', '${escapeHtml(donor.city)}')">
                    Record Donation
                </button>
            </td>
        </tr>
    `).join('');
}

function renderAllDonorsTable() {
    const tbody = document.getElementById('all-donors-tbody');
    if (!tbody) return;

    if (state.donors.length === 0) {
        tbody.innerHTML = `<tr class="empty-row"><td colspan="6">No registered donors yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = state.donors.map(d => {
        const isAvail = d.available;
        const statusBadge = isAvail 
            ? '<span class="badge badge-success">Available</span>' 
            : '<span class="badge badge-danger">Unavailable</span>';

        return `
            <tr>
                <td>#${d.id}</td>
                <td><strong>${escapeHtml(d.name)}</strong></td>
                <td><span class="blood-badge">${escapeHtml(d.bloodGroup ? d.bloodGroup.groupName : '-')}</span></td>
                <td>${escapeHtml(d.city)}</td>
                <td>${statusBadge}</td>
                <td>${formatDate(d.lastDonationDate)}</td>
            </tr>
        `;
    }).join('');
}

function renderDonationDonorDropdown() {
    const select = document.getElementById('donation-donor-select');
    if (!select) return;

    let html = '<option value="">-- Choose Registered Donor --</option>';
    state.donors.forEach(d => {
        const bg = d.bloodGroup ? d.bloodGroup.groupName : '';
        const availText = d.available ? '(Available)' : '(Recently Donated / Ineligible)';
        html += `<option value="${d.id}">#${d.id} - ${escapeHtml(d.name)} [${escapeHtml(bg)}] (${escapeHtml(d.city)}) ${availText}</option>`;
    });

    select.innerHTML = html;
}

function renderDonationHistoryTable() {
    const tbody = document.getElementById('donation-history-tbody');
    if (!tbody) return;

    if (state.donations.length === 0) {
        tbody.innerHTML = `<tr class="empty-row"><td colspan="4">No donation records found.</td></tr>`;
        return;
    }

    tbody.innerHTML = state.donations.map(dr => `
        <tr>
            <td>#${dr.id}</td>
            <td><strong>${escapeHtml(dr.donor ? dr.donor.name : 'Unknown')}</strong></td>
            <td><span class="blood-badge">${escapeHtml(dr.donor && dr.donor.bloodGroup ? dr.donor.bloodGroup.groupName : '-')}</span></td>
            <td>${formatDate(dr.donationDate)}</td>
        </tr>
    `).join('');
}

function updateStats() {
    const totalDonorsEl = document.getElementById('stat-total-donors');
    const availDonorsEl = document.getElementById('stat-available-donors');
    const bloodGroupsEl = document.getElementById('stat-blood-groups');
    const totalDonationsEl = document.getElementById('stat-total-donations');

    if (totalDonorsEl) totalDonorsEl.innerText = state.donors.length;
    if (availDonorsEl) availDonorsEl.innerText = state.donors.filter(d => d.available).length;
    if (bloodGroupsEl) bloodGroupsEl.innerText = state.bloodGroups.length;
    if (totalDonationsEl) totalDonationsEl.innerText = state.donations.length;
}
