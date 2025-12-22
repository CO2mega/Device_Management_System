import api from './index';

export const authApi = {
  login(credentials) {
    return api.post('/auth/login', credentials);
  },
  
  register(userData) {
    return api.post('/auth/register', userData);
  }
};

export const deviceApi = {
  getAll(params = {}) {
    return api.get('/devices', { params });
  },
  
  getById(id) {
    return api.get(`/devices/${id}`);
  },
  
  getTypes() {
    return api.get('/devices/types');
  },

  create(device) {
    return api.post('/devices', device);
  },
  
  update(id, device) {
    return api.put(`/devices/${id}`, device);
  },
  
  delete(id) {
    return api.delete(`/devices/${id}`);
  }
};

export const userApi = {
  getAll(params = {}) {
    return api.get('/users', { params });
  },
  
  getById(id) {
    return api.get(`/users/${id}`);
  },
  
  create(user) {
    return api.post('/users', user);
  },
  
  update(id, user) {
    return api.put(`/users/${id}`, user);
  },
  
  delete(id) {
    return api.delete(`/users/${id}`);
  }
};

export const loanApi = {
  getAll(params = {}) {
    return api.get('/loans', { params });
  },
  
  getById(id) {
    return api.get(`/loans/${id}`);
  },
  
  apply(request) {
    return api.post('/loans', request);
  },
  
  approve(id) {
    return api.put(`/loans/${id}/approve`);
  },
  
  reject(id, reason) {
    return api.put(`/loans/${id}/reject`, { rejectionReason: reason });
  },
  
  returnDevice(id) {
    return api.post(`/loans/${id}/return`);
  }
};

export const dashboardApi = {
  getStatistics() {
    return api.get('/dashboard/statistics');
  },

  getStatusDistribution() {
    return api.get('/dashboard/status-distribution');
  }
};
