import { useState, useEffect } from 'react';
import { getDepartments, createDepartment, updateDepartment, deleteDepartment } from '../services/api';

function Departments({ company }) {
    const [departments, setDepartments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        team: '',
        product: '',
        description: '',
        aiUsageWeight: 0.5,
        employeeCount: 10
    });

    useEffect(() => {
        loadDepartments();
    }, [company.id]);

    const loadDepartments = async () => {
        try {
            setLoading(true);
            const data = await getDepartments(company.id);
            setDepartments(data);
        } catch (error) {
            console.error('Error loading departments:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingId) {
                await updateDepartment(editingId, formData);
            } else {
                await createDepartment(company.id, formData);
            }
            resetForm();
            loadDepartments();
        } catch (error) {
            console.error('Error saving department:', error);
        }
    };

    const handleEdit = (dept) => {
        setEditingId(dept.id);
        setFormData({
            name: dept.name,
            team: dept.team || '',
            product: dept.product || '',
            description: dept.description || '',
            aiUsageWeight: dept.aiUsageWeight,
            employeeCount: dept.employeeCount
        });
        setShowForm(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this department?')) {
            try {
                await deleteDepartment(id);
                loadDepartments();
            } catch (error) {
                console.error('Error deleting department:', error);
            }
        }
    };

    const resetForm = () => {
        setShowForm(false);
        setEditingId(null);
        setFormData({
            name: '',
            team: '',
            product: '',
            description: '',
            aiUsageWeight: 0.5,
            employeeCount: 10
        });
    };

    if (loading) {
        return <div className="loading"><div className="spinner"></div></div>;
    }

    return (
        <div className="departments">
            <div className="page-header">
                <h1 className="page-title">Department Management</h1>
                <p className="page-subtitle">Configure departments and their AI usage weights for attribution</p>
            </div>

            {/* Add Button */}
            <div style={{ marginBottom: '1.5rem' }}>
                <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
                    {showForm ? '✕ Cancel' : '+ Add Department'}
                </button>
            </div>

            {/* Form */}
            {showForm && (
                <div className="card" style={{ marginBottom: '1.5rem' }}>
                    <h3 className="chart-title">{editingId ? 'Edit Department' : 'New Department'}</h3>
                    <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
                        <div className="form-group">
                            <label className="form-label">Department Name *</label>
                            <input
                                type="text"
                                className="form-input"
                                value={formData.name}
                                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                placeholder="e.g., Machine Learning"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Team</label>
                            <input
                                type="text"
                                className="form-input"
                                value={formData.team}
                                onChange={(e) => setFormData({ ...formData, team: e.target.value })}
                                placeholder="e.g., ML Engineering"
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Product</label>
                            <input
                                type="text"
                                className="form-input"
                                value={formData.product}
                                onChange={(e) => setFormData({ ...formData, product: e.target.value })}
                                placeholder="e.g., AI Platform"
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">AI Usage Weight (0-1) *</label>
                            <input
                                type="range"
                                min="0"
                                max="1"
                                step="0.05"
                                value={formData.aiUsageWeight}
                                onChange={(e) => setFormData({ ...formData, aiUsageWeight: parseFloat(e.target.value) })}
                                style={{ width: '100%' }}
                            />
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                                <span>Low AI (0)</span>
                                <span style={{ color: 'var(--color-primary)', fontWeight: 600 }}>{(formData.aiUsageWeight * 100).toFixed(0)}%</span>
                                <span>High AI (1)</span>
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label">Employee Count</label>
                            <input
                                type="number"
                                className="form-input"
                                value={formData.employeeCount}
                                onChange={(e) => setFormData({ ...formData, employeeCount: parseInt(e.target.value) || 0 })}
                                min="1"
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Description</label>
                            <input
                                type="text"
                                className="form-input"
                                value={formData.description}
                                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                placeholder="Brief description..."
                            />
                        </div>

                        <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '1rem' }}>
                            <button type="submit" className="btn btn-primary">
                                {editingId ? 'Update' : 'Create'} Department
                            </button>
                            <button type="button" className="btn btn-secondary" onClick={resetForm}>
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Department Cards */}
            <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))' }}>
                {departments.map((dept) => (
                    <div key={dept.id} className="card" style={{ position: 'relative' }}>
                        <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: '4px', background: `linear-gradient(90deg, var(--color-primary) ${dept.aiUsageWeight * 100}%, var(--color-border) ${dept.aiUsageWeight * 100}%)`, borderRadius: '8px 8px 0 0' }} />

                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                            <div>
                                <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '0.25rem' }}>{dept.name}</h3>
                                {dept.team && <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>{dept.team}</p>}
                            </div>
                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                                <button
                                    className="btn btn-secondary"
                                    style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                                    onClick={() => handleEdit(dept)}
                                >
                                    ✏️
                                </button>
                                <button
                                    className="btn btn-secondary"
                                    style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                                    onClick={() => handleDelete(dept.id)}
                                >
                                    🗑️
                                </button>
                            </div>
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                            <div>
                                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>AI Usage Weight</div>
                                <div style={{ fontSize: '1.25rem', fontWeight: 600, color: 'var(--color-primary)' }}>
                                    {(dept.aiUsageWeight * 100).toFixed(0)}%
                                </div>
                            </div>
                            <div>
                                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>Employees</div>
                                <div style={{ fontSize: '1.25rem', fontWeight: 600 }}>{dept.employeeCount}</div>
                            </div>
                        </div>

                        {dept.product && (
                            <div style={{ marginTop: '0.75rem' }}>
                                <span className="badge badge-success">{dept.product}</span>
                            </div>
                        )}

                        {dept.description && (
                            <p style={{ marginTop: '0.75rem', fontSize: '0.8125rem', color: 'var(--color-text-muted)' }}>
                                {dept.description}
                            </p>
                        )}
                    </div>
                ))}
            </div>

            {departments.length === 0 && (
                <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
                    <p style={{ fontSize: '2rem', marginBottom: '1rem' }}>🏢</p>
                    <p>No departments configured yet.</p>
                    <button className="btn btn-primary" onClick={() => setShowForm(true)} style={{ marginTop: '1rem' }}>
                        Add Your First Department
                    </button>
                </div>
            )}
        </div>
    );
}

export default Departments;
