requireLogin();

document.getElementById('welcome').textContent = 'Hello, ' + (localStorage.getItem(USER_KEY) || '') + '  ';

const tbody = document.getElementById('studentBody');

function cell(text) {
    const td = document.createElement('td');
    td.textContent = text;          // textContent (not innerHTML) keeps the page safe from injected HTML
    return td;
}

function render(students) {
    tbody.innerHTML = '';

    if (students.length === 0) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 7;
        td.className = 'empty';
        td.textContent = 'No students found';
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }

    students.forEach(function (s) {
        const tr = document.createElement('tr');
        tr.appendChild(cell(s.id));
        tr.appendChild(cell(s.fullName));
        tr.appendChild(cell(s.email));
        tr.appendChild(cell(s.age));
        tr.appendChild(cell(s.course));
        tr.appendChild(cell(s.phoneNumber));

        const actions = document.createElement('td');

        const edit = document.createElement('a');
        edit.href = 'edit-student.html?id=' + s.id;
        edit.className = 'btn btn-small';
        edit.textContent = 'Edit';

        const del = document.createElement('button');
        del.className = 'btn-danger btn-small';
        del.textContent = 'Delete';
        del.style.marginLeft = '6px';
        del.addEventListener('click', function () { deleteStudent(s.id, s.fullName); });

        actions.appendChild(edit);
        actions.appendChild(del);
        tr.appendChild(actions);
        tbody.appendChild(tr);
    });
}

// View all students  (GET /api/students)
async function loadStudents() {
    hideMessage('msg');
    const res = await authFetch('/api/students');
    if (res.ok) {
        render(res.data);
    } else {
        showMessage('msg', errorText(res.data, 'Could not load students'), 'error');
    }
}

// View one student by ID  (GET /api/students/{id})
async function searchById() {
    hideMessage('msg');
    const id = document.getElementById('searchId').value;
    if (!id) {
        showMessage('msg', 'Enter a student ID first', 'error');
        return;
    }
    const res = await authFetch('/api/students/' + encodeURIComponent(id));
    if (res.ok) {
        render([res.data]);
    } else {
        render([]);
        showMessage('msg', errorText(res.data, 'Student not found'), 'error');
    }
}

// Delete  (DELETE /api/students/{id})
async function deleteStudent(id, name) {
    if (!confirm('Delete student "' + name + '"?')) return;

    const res = await authFetch('/api/students/' + id, { method: 'DELETE' });
    if (res.ok) {
        await loadStudents();
        showMessage('msg', 'Student deleted successfully', 'success');
    } else {
        showMessage('msg', errorText(res.data, 'Delete failed'), 'error');
    }
}

// Logout  (POST /api/auth/logout) then clear the browser's token
async function logout() {
    try {
        await apiFetch('/api/auth/logout', { method: 'POST' });   // server puts the token on the denylist
    } catch (e) { /* even if this fails, still clear locally */ }
    clearSession();
    window.location.href = 'login.html?loggedout=1';
}

document.getElementById('searchBtn').addEventListener('click', searchById);
document.getElementById('showAllBtn').addEventListener('click', function () {
    document.getElementById('searchId').value = '';
    loadStudents();
});
document.getElementById('logoutBtn').addEventListener('click', logout);

// Show a message if we just came back from add/edit
const p = new URLSearchParams(window.location.search);
loadStudents().then(function () {
    if (p.get('added')) showMessage('msg', 'Student added successfully', 'success');
    if (p.get('updated')) showMessage('msg', 'Student updated successfully', 'success');
});
