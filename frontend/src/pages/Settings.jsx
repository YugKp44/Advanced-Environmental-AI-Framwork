import { useState, useEffect } from 'react';
import { updateCompany, createCompany, getDefaultCarbonIntensities, getCompanyCarbonConfigs, configureCarbonIntensity } from '../services/api';

function Settings({ company, onUpdate }) {
    const [activeTab, setActiveTab] = useState('company');
    const [formData, setFormData] = useState({
        name: '',
        industry: '',
        country: '',
        region: '',
        baseAiPercentage: 0.30,
        electricityCostPerKwh: 0.12,
        currency: 'USD'
    });
    const [defaultIntensities, setDefaultIntensities] = useState([]);
    const [companyConfigs, setCompanyConfigs] = useState([]);
    const [saving, setSaving] = useState(false);
    const [message, setMessage] = useState(null);

    useEffect(() => {
        if (company) {
            setFormData({
                name: company.name || '',
                industry: company.industry || '',
                country: company.country || '',
                region: company.region || '',
                baseAiPercentage: company.baseAiPercentage || 0.30,
                electricityCostPerKwh: company.electricityCostPerKwh || 0.12,
                currency: company.currency || 'USD'
            });
        }
        loadCarbonData();
    }, [company]);

    const loadCarbonData = async () => {
        try {
            const [defaults, configs] = await Promise.all([
                getDefaultCarbonIntensities(),
                company?.id ? getCompanyCarbonConfigs(company.id) : Promise.resolve([])
            ]);
            setDefaultIntensities(defaults);
            setCompanyConfigs(configs);
        } catch (error) {
            console.error('Error loading carbon data:', error);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        setMessage(null);

        try {
            if (company?.id) {
                await updateCompany(company.id, formData);
                setMessage({ type: 'success', text: 'Company settings updated successfully!' });
            } else {
                await createCompany(formData);
                setMessage({ type: 'success', text: 'Company created successfully!' });
            }
            onUpdate();
        } catch (error) {
            setMessage({ type: 'error', text: 'Error saving: ' + error.message });
        } finally {
            setSaving(false);
        }
    };

    const handleCarbonConfig = async (region, intensity) => {
        try {
            await configureCarbonIntensity(company.id, { region, carbonIntensity: intensity });
            loadCarbonData();
            setMessage({ type: 'success', text: `Carbon intensity for ${region} updated!` });
        } catch (error) {
            setMessage({ type: 'error', text: 'Error updating carbon config' });
        }
    };

    return (
        <div className="settings">
            <div className="page-header">
                <h1 className="page-title">⚙️ Settings</h1>
                <p className="page-subtitle">Configure company profile and carbon intensity factors</p>
            </div>

            {/* Tabs */}
            <div className="tabs">
                <button className={`tab ${activeTab === 'company' ? 'active' : ''}`} onClick={() => setActiveTab('company')}>
                    🏢 Company Profile
                </button>
                <button className={`tab ${activeTab === 'carbon' ? 'active' : ''}`} onClick={() => setActiveTab('carbon')}>
                    🌍 Carbon Intensity
                </button>
                <button className={`tab ${activeTab === 'about' ? 'active' : ''}`} onClick={() => setActiveTab('about')}>
                    ℹ️ About
                </button>
            </div>

            {/* Message */}
            {message && (
                <div className={`alert-item ${message.type === 'success' ? 'info' : 'critical'}`} style={{ marginBottom: '1rem' }}>
                    <span className="alert-icon">{message.type === 'success' ? '✅' : '❌'}</span>
                    <div className="alert-content">{message.text}</div>
                </div>
            )}

            {/* Company Profile */}
            {activeTab === 'company' && (
                <div className="card">
                    <h3 className="chart-title">Company Information</h3>
                    <form onSubmit={handleSubmit}>
                        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
                            <div className="form-group">
                                <label className="form-label">Company Name *</label>
                                <input
                                    type="text"
                                    className="form-input"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    placeholder="e.g., TechCorp AI Solutions"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Industry</label>
                                <select
                                    className="form-select"
                                    value={formData.industry}
                                    onChange={(e) => setFormData({ ...formData, industry: e.target.value })}
                                >
                                    <option value="">Select Industry</option>
                                    <option value="Technology">Technology</option>
                                    <option value="Finance">Finance</option>
                                    <option value="Healthcare">Healthcare</option>
                                    <option value="Manufacturing">Manufacturing</option>
                                    <option value="Retail">Retail</option>
                                    <option value="Other">Other</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Country</label>
                                <input
                                    type="text"
                                    className="form-input"
                                    value={formData.country}
                                    onChange={(e) => setFormData({ ...formData, country: e.target.value })}
                                    placeholder="e.g., United States"
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Primary Region</label>
                                <select
                                    className="form-select"
                                    value={formData.region}
                                    onChange={(e) => setFormData({ ...formData, region: e.target.value })}
                                >
                                    <option value="">Select Region</option>
                                    <option value="US">United States</option>
                                    <option value="EU">European Union</option>
                                    <option value="UK">United Kingdom</option>
                                    <option value="IN">India</option>
                                    <option value="AU">Australia</option>
                                    <option value="CA">Canada</option>
                                    <option value="NO">Norway</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Base AI Usage Percentage</label>
                                <input
                                    type="range"
                                    min="0"
                                    max="1"
                                    step="0.05"
                                    value={formData.baseAiPercentage}
                                    onChange={(e) => setFormData({ ...formData, baseAiPercentage: parseFloat(e.target.value) })}
                                    style={{ width: '100%' }}
                                />
                                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem' }}>
                                    <span style={{ color: 'var(--color-text-muted)' }}>0%</span>
                                    <span style={{ color: 'var(--color-primary)', fontWeight: 600 }}>{(formData.baseAiPercentage * 100).toFixed(0)}%</span>
                                    <span style={{ color: 'var(--color-text-muted)' }}>100%</span>
                                </div>
                                <p style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '0.25rem' }}>
                                    Percentage of total energy attributed to AI workloads
                                </p>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Electricity Cost (per kWh)</label>
                                <div style={{ display: 'flex', gap: '0.5rem' }}>
                                    <select
                                        className="form-select"
                                        value={formData.currency}
                                        onChange={(e) => setFormData({ ...formData, currency: e.target.value })}
                                        style={{ width: '80px' }}
                                    >
                                        <option value="USD">$</option>
                                        <option value="EUR">€</option>
                                        <option value="GBP">£</option>
                                        <option value="INR">₹</option>
                                    </select>
                                    <input
                                        type="number"
                                        className="form-input"
                                        value={formData.electricityCostPerKwh}
                                        onChange={(e) => setFormData({ ...formData, electricityCostPerKwh: parseFloat(e.target.value) })}
                                        step="0.01"
                                        min="0"
                                    />
                                </div>
                            </div>
                        </div>

                        <button type="submit" className="btn btn-primary" disabled={saving} style={{ marginTop: '1.5rem' }}>
                            {saving ? '⏳ Saving...' : '💾 Save Settings'}
                        </button>
                    </form>
                </div>
            )}

            {/* Carbon Intensity */}
            {activeTab === 'carbon' && (
                <div className="card">
                    <h3 className="chart-title">Carbon Intensity Factors (gCO₂/kWh)</h3>
                    <p style={{ color: 'var(--color-text-muted)', marginBottom: '1rem' }}>
                        Carbon intensity varies by region. Lower values = cleaner electricity grid.
                    </p>

                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Region</th>
                                <th>Name</th>
                                <th>Default Intensity</th>
                                <th>Custom Intensity</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {defaultIntensities.map((intensity, index) => {
                                const customConfig = companyConfigs.find(c => c.region === intensity.region);
                                return (
                                    <tr key={index}>
                                        <td>
                                            <span className="badge badge-success">{intensity.region}</span>
                                        </td>
                                        <td>{intensity.regionName}</td>
                                        <td style={{ fontFamily: 'var(--font-mono)' }}>{intensity.carbonIntensity} gCO₂/kWh</td>
                                        <td>
                                            <input
                                                type="number"
                                                className="form-input"
                                                defaultValue={customConfig?.carbonIntensity || ''}
                                                placeholder={intensity.carbonIntensity}
                                                style={{ width: '120px' }}
                                                onBlur={(e) => {
                                                    if (e.target.value && company?.id) {
                                                        handleCarbonConfig(intensity.region, parseFloat(e.target.value));
                                                    }
                                                }}
                                            />
                                        </td>
                                        <td>
                                            <span className={`badge ${customConfig ? 'badge-warning' : 'badge-success'}`}>
                                                {customConfig ? 'Custom' : 'Default'}
                                            </span>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            )}

            {/* About */}
            {activeTab === 'about' && (
                <div className="card">
                    <h3 className="chart-title">About EcoAI Framework</h3>

                    <div style={{ marginBottom: '1.5rem' }}>
                        <h4 style={{ marginBottom: '0.5rem' }}>🌱 AI Energy & Carbon Management Framework</h4>
                        <p style={{ color: 'var(--color-text-secondary)' }}>
                            A company-level framework for tracking, attributing, forecasting, and optimizing
                            electricity usage and carbon emissions caused by AI workloads.
                        </p>
                    </div>

                    <div style={{ marginBottom: '1.5rem' }}>
                        <h4 style={{ marginBottom: '0.5rem' }}>📊 Key Features</h4>
                        <ul style={{ color: 'var(--color-text-secondary)', paddingLeft: '1.5rem' }}>
                            <li>Company-Level Energy Tracking</li>
                            <li>AI Energy Attribution Engine</li>
                            <li>Carbon Emission Calculation (ESG-Ready)</li>
                            <li>Cost + Carbon Dual Impact Tracking</li>
                            <li>What-If Simulation Engine</li>
                            <li>Analytics & Forecasting</li>
                            <li>Actionable Insights & Alerts</li>
                            <li>Executive-Friendly Dashboard</li>
                        </ul>
                    </div>

                    <div style={{ marginBottom: '1.5rem' }}>
                        <h4 style={{ marginBottom: '0.5rem' }}>⚠️ Important Note</h4>
                        <p style={{ color: 'var(--color-text-secondary)' }}>
                            This is an <strong>estimation and management framework</strong> for decision-making.
                            Values are calculated using industry-standard formulas and configurable factors.
                            Actual energy consumption and emissions may vary.
                        </p>
                    </div>

                    <div style={{
                        padding: '1rem',
                        background: 'var(--color-primary-glow)',
                        borderRadius: 'var(--radius-md)',
                        marginTop: '1rem'
                    }}>
                        <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--color-text-primary)' }}>
                            <strong>Tech Stack:</strong> Java 17 • Spring Boot • PostgreSQL • React • Recharts
                        </p>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Settings;
