import axios from 'axios';

// Use environment variable for production, fallback to localhost for development
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// ==================== Company APIs ====================
export const getCompanies = async () => {
    const response = await api.get('/companies');
    return response.data;
};

export const getCompany = async (id) => {
    const response = await api.get(`/companies/${id}`);
    return response.data;
};

export const createCompany = async (data) => {
    const response = await api.post('/companies', data);
    return response.data;
};

export const updateCompany = async (id, data) => {
    const response = await api.put(`/companies/${id}`, data);
    return response.data;
};

// ==================== Department APIs ====================
export const getDepartments = async (companyId) => {
    const response = await api.get(`/companies/${companyId}/departments`);
    return response.data;
};

export const createDepartment = async (companyId, data) => {
    const response = await api.post(`/companies/${companyId}/departments`, data);
    return response.data;
};

export const updateDepartment = async (id, data) => {
    const response = await api.put(`/departments/${id}`, data);
    return response.data;
};

export const deleteDepartment = async (id) => {
    await api.delete(`/departments/${id}`);
};

// ==================== Energy Usage APIs ====================
export const getEnergyUsage = async (companyId, startDate, endDate) => {
    let url = `/companies/${companyId}/energy`;
    if (startDate && endDate) {
        url += `?startDate=${startDate}&endDate=${endDate}`;
    }
    const response = await api.get(url);
    return response.data;
};

export const recordEnergyUsage = async (companyId, data) => {
    const response = await api.post(`/companies/${companyId}/energy`, data);
    return response.data;
};

export const uploadEnergyCsv = async (companyId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post(`/companies/${companyId}/energy/csv`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
};

export const getEnergyTrends = async (companyId, months = 6) => {
    const response = await api.get(`/companies/${companyId}/energy/trends?months=${months}`);
    return response.data;
};

// ==================== Dashboard APIs ====================
export const getDashboard = async (companyId) => {
    const response = await api.get(`/companies/${companyId}/dashboard`);
    return response.data;
};

export const getDashboardKpis = async (companyId) => {
    const response = await api.get(`/companies/${companyId}/dashboard/kpis`);
    return response.data;
};

export const getDepartmentBreakdown = async (companyId) => {
    const response = await api.get(`/companies/${companyId}/dashboard/departments`);
    return response.data;
};

// ==================== Carbon APIs ====================
export const getDefaultCarbonIntensities = async () => {
    const response = await api.get('/carbon/intensities');
    return response.data;
};

export const getCompanyCarbonConfigs = async (companyId) => {
    const response = await api.get(`/companies/${companyId}/carbon/config`);
    return response.data;
};

export const configureCarbonIntensity = async (companyId, data) => {
    const response = await api.post(`/companies/${companyId}/carbon/config`, data);
    return response.data;
};

// ==================== Simulation APIs ====================
export const simulateGrowth = async (companyId, growthPercent, monthsAhead = 12) => {
    const response = await api.post(`/companies/${companyId}/simulate/growth`, {
        growthPercent,
        monthsAhead,
    });
    return response.data;
};

export const simulateRegionChange = async (companyId, fromRegion, toRegion) => {
    const response = await api.post(`/companies/${companyId}/simulate/region`, {
        fromRegion,
        toRegion,
    });
    return response.data;
};

export const simulateEfficiency = async (companyId, efficiencyPercent) => {
    const response = await api.post(`/companies/${companyId}/simulate/efficiency`, {
        efficiencyPercent,
    });
    return response.data;
};

export const getSavedScenarios = async (companyId) => {
    const response = await api.get(`/companies/${companyId}/simulate/scenarios`);
    return response.data;
};

// ==================== Analytics APIs ====================
export const getHistoricalTrends = async (companyId, months = 6) => {
    const response = await api.get(`/companies/${companyId}/analytics/trends?months=${months}`);
    return response.data;
};

export const getForecast = async (companyId, months = 3) => {
    const response = await api.get(`/companies/${companyId}/analytics/forecast?months=${months}`);
    return response.data;
};

export const getDepartmentComparison = async (companyId) => {
    const response = await api.get(`/companies/${companyId}/analytics/comparison`);
    return response.data;
};

export const getYearOverYear = async (companyId) => {
    const response = await api.get(`/companies/${companyId}/analytics/yoy`);
    return response.data;
};

// ==================== Alerts APIs ====================
export const getActiveAlerts = async (companyId) => {
    const response = await api.get(`/companies/${companyId}/alerts`);
    return response.data;
};

export const getAllThresholds = async (companyId) => {
    const response = await api.get(`/companies/${companyId}/alerts/thresholds`);
    return response.data;
};

export const configureThreshold = async (companyId, data) => {
    const response = await api.post(`/companies/${companyId}/alerts/thresholds`, data);
    return response.data;
};

export const getOptimizationInsights = async (companyId) => {
    const response = await api.get(`/companies/${companyId}/insights`);
    return response.data;
};

export default api;
