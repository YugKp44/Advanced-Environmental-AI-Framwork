import { useState, useEffect } from 'react';
import { getActiveAlerts, getAllThresholds, configureThreshold, getOptimizationInsights } from '../services/api';

function Alerts({ company }) {
    const [alerts, setAlerts] = useState([]);
    const [thresholds, setThresholds] = useState([]);
    const [insights, setInsights] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showThresholdForm, setShowThresholdForm] = useState(false);
    const [formData, setFormData] = useState({
        metricType: 'AI_USAGE_KWH',
        thresholdValue: 10000,
        message: ''
    });

    useEffect(() => {
        loadData();
    }, [company.id]);

    const loadData = async () => {
        try {
            setLoading(true);
            const [alertsData, thresholdsData, insightsData] = await Promise.all([
                getActiveAlerts(company.id),
                getAllThresholds(company.id),
                getOptimizationInsights(company.id)
            ]);
            setAlerts(alertsData);
            setThresholds(thresholdsData);
            setInsights(insightsData);
        } catch (error) {
            console.error('Error loading alerts:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmitThreshold = async (e) => {
        e.preventDefault();
        try {
            await configureThreshold(company.id, formData);
            setShowThresholdForm(false);
            setFormData({ metricType: 'AI_USAGE_KWH', thresholdValue: 10000, message: '' });
            loadData();
        } catch (error) {
            console.error('Error saving threshold:', error);
        }
    };

    const metricTypes = [
        { value: 'AI_USAGE_KWH', label: 'AI Usage (kWh)', unit: 'kWh' },
        { value: 'TOTAL_ENERGY_KWH', label: 'Total Energy (kWh)', unit: 'kWh' },
        { value: 'CARBON_EMISSION_KG', label: 'Carbon Emissions (kg)', unit: 'kg CO₂e' },
        { value: 'MONTHLY_COST', label: 'Monthly Cost', unit: '$' },
    ];

    const getCategoryIcon = (category) => {
        switch (category) {
            case 'REGION': return '🌍';
            case 'BATCHING': return '📦';
            case 'EFFICIENCY': return '⚡';
            case 'SCHEDULING': return '📅';
            case 'CARBON_BUDGET': return '🌱';
            default: return '💡';
        }
    };

    const getPriorityColor = (priority) => {
        switch (priority) {
            case 'HIGH': return 'var(--color-error)';
            case 'MEDIUM': return 'var(--color-warning)';
            case 'LOW': return 'var(--color-info)';
            default: return 'var(--color-text-muted)';
        }
    };

    if (loading) {
        return <div className="loading"><div className="spinner"></div></div>;
    }

    return (
        <div className="alerts-page">
            <div className="page-header">
                <h1 className="page-title">🔔 Alerts & Insights</h1>
                <p className="page-subtitle">Monitor thresholds and get optimization recommendations</p>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                {/* Active Alerts */}
                <div className="card">
                    <div className="card-header">
                        <h3 className="chart-title">⚠️ Active Alerts</h3>
                        <span className="badge badge-warning">{alerts.length} active</span>
                    </div>

                    {alerts.length > 0 ? (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                            {alerts.map((alert, index) => (
                                <div
                                    key={index}
                                    className={`alert-item ${alert.severity?.toLowerCase()}`}
                                    style={{ margin: 0 }}
                                >
                                    <span className="alert-icon">
                                        {alert.severity === 'CRITICAL' ? '🔴' : alert.severity === 'WARNING' ? '🟡' : '🔵'}
                                    </span>
                                    <div className="alert-content">
                                        <div className="alert-title">{alert.alertTitle}</div>
                                        <div className="alert-message">{alert.alertMessage}</div>
                                        <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem', fontSize: '0.75rem' }}>
                                            <span style={{ color: 'var(--color-text-muted)' }}>
                                                Current: <strong style={{ color: 'var(--color-text-primary)' }}>{alert.currentValue?.toLocaleString()}</strong>
                                            </span>
                                            <span style={{ color: 'var(--color-text-muted)' }}>
                                                Threshold: <strong style={{ color: 'var(--color-text-primary)' }}>{alert.thresholdValue?.toLocaleString()}</strong>
                                            </span>
                                        </div>
                                    </div>
                                    <div style={{ textAlign: 'right' }}>
                                        <div style={{ fontSize: '1.25rem', fontWeight: 700, color: alert.severity === 'CRITICAL' ? 'var(--color-error)' : 'var(--color-warning)' }}>
                                            {alert.percentOfThreshold?.toFixed(0)}%
                                        </div>
                                        <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>of threshold</div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--color-text-muted)' }}>
                            <p style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>✅</p>
                            <p>All systems normal. No active alerts.</p>
                        </div>
                    )}
                </div>

                {/* Threshold Configuration */}
                <div className="card">
                    <div className="card-header">
                        <h3 className="chart-title">⚙️ Threshold Configuration</h3>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setShowThresholdForm(!showThresholdForm)}
                            style={{ padding: '0.25rem 0.75rem', fontSize: '0.875rem' }}
                        >
                            {showThresholdForm ? '✕' : '+ Add'}
                        </button>
                    </div>

                    {showThresholdForm && (
                        <form onSubmit={handleSubmitThreshold} style={{ marginBottom: '1rem', padding: '1rem', background: 'var(--color-surface-elevated)', borderRadius: 'var(--radius-md)' }}>
                            <div className="form-group">
                                <label className="form-label">Metric Type</label>
                                <select
                                    className="form-select"
                                    value={formData.metricType}
                                    onChange={(e) => setFormData({ ...formData, metricType: e.target.value })}
                                >
                                    {metricTypes.map(m => (
                                        <option key={m.value} value={m.value}>{m.label}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group">
                                <label className="form-label">Threshold Value</label>
                                <input
                                    type="number"
                                    className="form-input"
                                    value={formData.thresholdValue}
                                    onChange={(e) => setFormData({ ...formData, thresholdValue: parseFloat(e.target.value) })}
                                    placeholder="e.g., 10000"
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Alert Message (Optional)</label>
                                <input
                                    type="text"
                                    className="form-input"
                                    value={formData.message}
                                    onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                                    placeholder="Custom alert message..."
                                />
                            </div>
                            <button type="submit" className="btn btn-primary">Save Threshold</button>
                        </form>
                    )}

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        {thresholds.map((threshold, index) => (
                            <div
                                key={index}
                                style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    padding: '0.75rem',
                                    background: 'var(--color-surface-elevated)',
                                    borderRadius: 'var(--radius-md)'
                                }}
                            >
                                <div>
                                    <div style={{ fontWeight: 500 }}>
                                        {metricTypes.find(m => m.value === threshold.metricType)?.label || threshold.metricType}
                                    </div>
                                    <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                                        {threshold.alertMessage || 'No custom message'}
                                    </div>
                                </div>
                                <div style={{ textAlign: 'right' }}>
                                    <div style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>
                                        {threshold.thresholdValue?.toLocaleString()}
                                    </div>
                                    <span className={`badge ${threshold.active ? 'badge-success' : 'badge-warning'}`}>
                                        {threshold.active ? 'Active' : 'Inactive'}
                                    </span>
                                </div>
                            </div>
                        ))}
                        {thresholds.length === 0 && (
                            <p style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: '1rem' }}>
                                No thresholds configured yet.
                            </p>
                        )}
                    </div>
                </div>
            </div>

            {/* Optimization Insights */}
            <div className="card" style={{ marginTop: '1.5rem' }}>
                <h3 className="chart-title">💡 Optimization Insights</h3>
                <p style={{ color: 'var(--color-text-muted)', marginBottom: '1rem' }}>
                    Data-driven recommendations to reduce energy consumption and carbon footprint
                </p>

                <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))' }}>
                    {insights.map((insight, index) => (
                        <div
                            key={index}
                            style={{
                                padding: '1.25rem',
                                background: 'var(--color-surface-elevated)',
                                borderRadius: 'var(--radius-lg)',
                                borderLeft: `4px solid ${getPriorityColor(insight.priority)}`
                            }}
                        >
                            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem' }}>
                                <span style={{ fontSize: '1.5rem' }}>{getCategoryIcon(insight.category)}</span>
                                <div style={{ flex: 1 }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                                        <h4 style={{ fontSize: '1rem', fontWeight: 600 }}>{insight.title}</h4>
                                        <span className={`badge ${insight.priority === 'HIGH' ? 'badge-error' : insight.priority === 'MEDIUM' ? 'badge-warning' : 'badge-success'}`}>
                                            {insight.priority}
                                        </span>
                                    </div>
                                    <p style={{ fontSize: '0.875rem', color: 'var(--color-text-secondary)', marginBottom: '0.75rem' }}>
                                        {insight.description}
                                    </p>
                                    <div style={{
                                        padding: '0.5rem 0.75rem',
                                        background: 'var(--color-primary-glow)',
                                        borderRadius: 'var(--radius-sm)',
                                        fontSize: '0.8125rem',
                                        color: 'var(--color-primary)'
                                    }}>
                                        <strong>Impact:</strong> {insight.impact}
                                    </div>
                                    {insight.actionable && (
                                        <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-muted)', marginTop: '0.5rem' }}>
                                            <strong>Action:</strong> {insight.actionable}
                                        </p>
                                    )}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default Alerts;
