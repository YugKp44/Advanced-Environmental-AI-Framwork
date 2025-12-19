import { useState, useEffect } from 'react';
import { getEnergyUsage, recordEnergyUsage, uploadEnergyCsv, getDepartments } from '../services/api';

function EnergyManagement({ company }) {
    const [energyData, setEnergyData] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [formData, setFormData] = useState({
        totalKwh: '',
        departmentId: '',
        usageDate: new Date().toISOString().split('T')[0],
        region: company.region || 'US',
        periodType: 'DAILY'
    });
    const [uploadStatus, setUploadStatus] = useState(null);

    useEffect(() => {
        loadData();
    }, [company.id]);

    const loadData = async () => {
        try {
            setLoading(true);
            const [energy, depts] = await Promise.all([
                getEnergyUsage(company.id),
                getDepartments(company.id)
            ]);
            setEnergyData(energy);
            setDepartments(depts);
        } catch (error) {
            console.error('Error loading data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await recordEnergyUsage(company.id, {
                ...formData,
                totalKwh: parseFloat(formData.totalKwh),
                departmentId: formData.departmentId || null
            });
            setShowForm(false);
            setFormData({
                totalKwh: '',
                departmentId: '',
                usageDate: new Date().toISOString().split('T')[0],
                region: company.region || 'US',
                periodType: 'DAILY'
            });
            loadData();
        } catch (error) {
            console.error('Error recording energy:', error);
        }
    };

    const handleFileUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        try {
            setUploadStatus({ loading: true, message: 'Uploading...' });
            const result = await uploadEnergyCsv(company.id, file);
            setUploadStatus({
                loading: false,
                success: result.success,
                message: `Successfully imported ${result.recordsImported} records`
            });
            loadData();
        } catch (error) {
            setUploadStatus({
                loading: false,
                success: false,
                message: 'Upload failed: ' + error.message
            });
        }
    };

    const formatDate = (dateStr) => {
        return new Date(dateStr).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    };

    if (loading) {
        return <div className="loading"><div className="spinner"></div></div>;
    }

    return (
        <div className="energy-management">
            <div className="page-header">
                <h1 className="page-title">Energy Usage Management</h1>
                <p className="page-subtitle">Track and manage electricity consumption data</p>
            </div>

            {/* Actions */}
            <div className="card" style={{ marginBottom: '1.5rem' }}>
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
                    <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
                        {showForm ? '✕ Cancel' : '+ Add Energy Record'}
                    </button>

                    <div className="file-upload" style={{ padding: '0.5rem 1rem', flex: 1, minWidth: '200px' }}>
                        <label style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <span>📁</span>
                            <span>Upload CSV</span>
                            <input
                                type="file"
                                accept=".csv"
                                onChange={handleFileUpload}
                                style={{ display: 'none' }}
                            />
                        </label>
                    </div>

                    {uploadStatus && (
                        <span className={`badge ${uploadStatus.success ? 'badge-success' : 'badge-error'}`}>
                            {uploadStatus.message}
                        </span>
                    )}
                </div>

                {/* Add Form */}
                {showForm && (
                    <form onSubmit={handleSubmit} style={{ marginTop: '1.5rem', display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
                        <div className="form-group">
                            <label className="form-label">Total kWh</label>
                            <input
                                type="number"
                                className="form-input"
                                value={formData.totalKwh}
                                onChange={(e) => setFormData({ ...formData, totalKwh: e.target.value })}
                                placeholder="e.g., 500"
                                required
                                step="0.01"
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Department (Optional)</label>
                            <select
                                className="form-select"
                                value={formData.departmentId}
                                onChange={(e) => setFormData({ ...formData, departmentId: e.target.value })}
                            >
                                <option value="">Company-wide</option>
                                {departments.map(dept => (
                                    <option key={dept.id} value={dept.id}>{dept.name}</option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
                            <label className="form-label">Date</label>
                            <input
                                type="date"
                                className="form-input"
                                value={formData.usageDate}
                                onChange={(e) => setFormData({ ...formData, usageDate: e.target.value })}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Region</label>
                            <select
                                className="form-select"
                                value={formData.region}
                                onChange={(e) => setFormData({ ...formData, region: e.target.value })}
                            >
                                <option value="US">United States</option>
                                <option value="EU">European Union</option>
                                <option value="IN">India</option>
                                <option value="UK">United Kingdom</option>
                                <option value="AU">Australia</option>
                                <option value="NO">Norway</option>
                            </select>
                        </div>

                        <div style={{ gridColumn: '1 / -1' }}>
                            <button type="submit" className="btn btn-primary">
                                Save Record
                            </button>
                        </div>
                    </form>
                )}
            </div>

            {/* CSV Format Help */}
            <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem' }}>
                <details>
                    <summary style={{ cursor: 'pointer', fontWeight: 600, color: 'var(--color-text-primary)' }}>
                        📋 CSV Format Guide
                    </summary>
                    <div style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: 'var(--color-text-secondary)' }}>
                        <p>Expected columns: <code>date,totalKwh,departmentName,region</code></p>
                        <p>Example:</p>
                        <pre style={{
                            background: 'var(--color-surface-elevated)',
                            padding: '0.5rem',
                            borderRadius: '4px',
                            marginTop: '0.5rem'
                        }}>
                            date,totalKwh,departmentName,region
                            2024-01-15,500.5,Machine Learning,US
                            2024-01-15,300.2,Data Science,US
                            2024-01-16,520.0,Machine Learning,US
                        </pre>
                    </div>
                </details>
            </div>

            {/* Energy Data Table */}
            <div className="card">
                <h3 className="chart-title">Energy Records ({energyData.length} entries)</h3>
                <div style={{ overflowX: 'auto' }}>
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Department</th>
                                <th>Total kWh</th>
                                <th>AI kWh</th>
                                <th>CO₂e (kg)</th>
                                <th>Cost</th>
                                <th>Region</th>
                                <th>Source</th>
                            </tr>
                        </thead>
                        <tbody>
                            {energyData.slice(0, 50).map((record) => (
                                <tr key={record.id}>
                                    <td>{formatDate(record.usageDate)}</td>
                                    <td>{record.departmentName || 'Company-wide'}</td>
                                    <td style={{ fontFamily: 'var(--font-mono)' }}>{record.totalKwh?.toFixed(2)}</td>
                                    <td style={{ fontFamily: 'var(--font-mono)', color: 'var(--color-primary)' }}>
                                        {record.aiAttributedKwh?.toFixed(2)}
                                    </td>
                                    <td style={{ fontFamily: 'var(--font-mono)' }}>{record.co2eKg?.toFixed(2)}</td>
                                    <td style={{ fontFamily: 'var(--font-mono)' }}>
                                        ${record.cost?.toFixed(2)}
                                    </td>
                                    <td>
                                        <span className="badge badge-success">{record.region}</span>
                                    </td>
                                    <td style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                                        {record.dataSource}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
                {energyData.length > 50 && (
                    <p style={{ marginTop: '1rem', fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>
                        Showing first 50 of {energyData.length} records
                    </p>
                )}
            </div>
        </div>
    );
}

export default EnergyManagement;
