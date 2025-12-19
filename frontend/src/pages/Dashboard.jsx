import { useState, useEffect } from 'react';
import {
    LineChart, Line, AreaChart, Area, PieChart, Pie, Cell,
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import { getDashboard } from '../services/api';

const COLORS = ['#10b981', '#3b82f6', '#8b5cf6', '#f59e0b', '#ec4899', '#6366f1'];

function Dashboard({ company }) {
    const [dashboardData, setDashboardData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadDashboard();
    }, [company.id]);

    const loadDashboard = async () => {
        try {
            setLoading(true);
            const data = await getDashboard(company.id);
            setDashboardData(data);
        } catch (error) {
            console.error('Error loading dashboard:', error);
        } finally {
            setLoading(false);
        }
    };

    const formatNumber = (num) => {
        if (num === null || num === undefined) return '0';
        if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M';
        if (num >= 1000) return (num / 1000).toFixed(1) + 'K';
        return num.toFixed(1);
    };

    const formatCurrency = (num) => {
        if (num === null || num === undefined) return '$0';
        return '$' + formatNumber(num);
    };

    if (loading) {
        return (
            <div className="loading">
                <div className="spinner"></div>
            </div>
        );
    }

    const summary = dashboardData?.summary || {};
    const trends = dashboardData?.trends || [];
    const departmentBreakdown = dashboardData?.departmentBreakdown || [];
    const alerts = dashboardData?.alerts || [];
    const insights = dashboardData?.insights || [];

    return (
        <div className="dashboard">
            <div className="page-header">
                <h1 className="page-title">Executive Dashboard</h1>
                <p className="page-subtitle">{company.name} • Last 30 Days Overview</p>
            </div>

            {/* KPI Cards */}
            <div className="kpi-grid">
                <div className="kpi-card energy">
                    <div className="kpi-icon">⚡</div>
                    <div className="kpi-label">Total Energy</div>
                    <div className="kpi-value">{formatNumber(summary.totalEnergyKwh)} kWh</div>
                    <div className={`kpi-change ${summary.energyChangePercent >= 0 ? 'negative' : 'positive'}`}>
                        {summary.energyChangePercent >= 0 ? '↑' : '↓'} {Math.abs(summary.energyChangePercent || 0).toFixed(1)}% vs prev period
                    </div>
                </div>

                <div className="kpi-card ai">
                    <div className="kpi-icon">🤖</div>
                    <div className="kpi-label">AI Energy Usage</div>
                    <div className="kpi-value">{formatNumber(summary.aiEnergyKwh)} kWh</div>
                    <div className="kpi-subtext" style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginTop: '0.25rem' }}>
                        {(summary.aiPercentage || 0).toFixed(1)}% of total
                    </div>
                </div>

                <div className="kpi-card carbon">
                    <div className="kpi-icon">🌍</div>
                    <div className="kpi-label">Carbon Emissions</div>
                    <div className="kpi-value">{formatNumber(summary.totalCo2eKg)} kg CO₂e</div>
                    <div className={`kpi-change ${summary.carbonChangePercent >= 0 ? 'negative' : 'positive'}`}>
                        {summary.carbonChangePercent >= 0 ? '↑' : '↓'} {Math.abs(summary.carbonChangePercent || 0).toFixed(1)}% vs prev period
                    </div>
                </div>

                <div className="kpi-card cost">
                    <div className="kpi-icon">💰</div>
                    <div className="kpi-label">Electricity Cost</div>
                    <div className="kpi-value">{formatCurrency(summary.totalCost)}</div>
                    <div className={`kpi-change ${summary.costChangePercent >= 0 ? 'negative' : 'positive'}`}>
                        {summary.costChangePercent >= 0 ? '↑' : '↓'} {Math.abs(summary.costChangePercent || 0).toFixed(1)}% vs prev period
                    </div>
                </div>
            </div>

            {/* Charts */}
            <div className="charts-grid">
                {/* Energy Trend Chart */}
                <div className="chart-card">
                    <h3 className="chart-title">Energy Trend (Last 6 Months)</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <AreaChart data={trends}>
                            <defs>
                                <linearGradient id="colorAi" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.3} />
                                    <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                                </linearGradient>
                                <linearGradient id="colorTotal" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3} />
                                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                                </linearGradient>
                            </defs>
                            <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                            <XAxis dataKey="period" stroke="#6b7280" fontSize={12} />
                            <YAxis stroke="#6b7280" fontSize={12} />
                            <Tooltip
                                contentStyle={{ background: '#1a2332', border: '1px solid #374151', borderRadius: '8px' }}
                                labelStyle={{ color: '#f9fafb' }}
                            />
                            <Legend />
                            <Area type="monotone" dataKey="totalEnergyKwh" stroke="#3b82f6" fillOpacity={1} fill="url(#colorTotal)" name="Total kWh" />
                            <Area type="monotone" dataKey="aiEnergyKwh" stroke="#10b981" fillOpacity={1} fill="url(#colorAi)" name="AI kWh" />
                        </AreaChart>
                    </ResponsiveContainer>
                </div>

                {/* Department Breakdown Pie */}
                <div className="chart-card">
                    <h3 className="chart-title">AI Usage by Department</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={departmentBreakdown}
                                dataKey="aiEnergyKwh"
                                nameKey="departmentName"
                                cx="50%"
                                cy="50%"
                                outerRadius={100}
                                label={({ name, percent }) => `${name} (${(percent * 100).toFixed(0)}%)`}
                                labelLine={false}
                            >
                                {departmentBreakdown.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Pie>
                            <Tooltip
                                contentStyle={{ background: '#1a2332', border: '1px solid #374151', borderRadius: '8px' }}
                            />
                        </PieChart>
                    </ResponsiveContainer>
                </div>
            </div>

            {/* Alerts & Insights */}
            <div className="charts-grid">
                {/* Active Alerts */}
                <div className="alerts-section">
                    <h3 className="chart-title">⚠️ Active Alerts</h3>
                    {alerts.length > 0 ? (
                        alerts.slice(0, 5).map((alert, index) => (
                            <div key={index} className={`alert-item ${alert.severity?.toLowerCase()}`}>
                                <span className="alert-icon">
                                    {alert.severity === 'CRITICAL' ? '🔴' : alert.severity === 'WARNING' ? '🟡' : '🔵'}
                                </span>
                                <div className="alert-content">
                                    <div className="alert-title">{alert.alertTitle}</div>
                                    <div className="alert-message">{alert.alertMessage}</div>
                                </div>
                                <span className="badge badge-warning">
                                    {alert.percentOfThreshold?.toFixed(0)}%
                                </span>
                            </div>
                        ))
                    ) : (
                        <p style={{ color: 'var(--color-text-muted)' }}>No active alerts. All systems normal.</p>
                    )}
                </div>

                {/* Insights */}
                <div className="card">
                    <h3 className="chart-title">💡 Optimization Insights</h3>
                    {insights.slice(0, 3).map((insight, index) => (
                        <div key={index} className="insight-card">
                            <div className="insight-icon">
                                {insight.category === 'REGION' ? '🌍' :
                                    insight.category === 'BATCHING' ? '📦' :
                                        insight.category === 'EFFICIENCY' ? '⚡' : '💡'}
                            </div>
                            <div className="insight-content">
                                <h4>{insight.title}</h4>
                                <p>{insight.description?.substring(0, 100)}...</p>
                                <div className="insight-impact">{insight.impact}</div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Cost vs Carbon Bar Chart */}
            <div className="chart-card" style={{ marginTop: '1.5rem' }}>
                <h3 className="chart-title">Cost vs Carbon by Department</h3>
                <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={departmentBreakdown}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                        <XAxis dataKey="departmentName" stroke="#6b7280" fontSize={12} />
                        <YAxis yAxisId="left" stroke="#6b7280" fontSize={12} />
                        <YAxis yAxisId="right" orientation="right" stroke="#6b7280" fontSize={12} />
                        <Tooltip
                            contentStyle={{ background: '#1a2332', border: '1px solid #374151', borderRadius: '8px' }}
                        />
                        <Legend />
                        <Bar yAxisId="left" dataKey="aiEnergyKwh" fill="#10b981" name="AI kWh" radius={[4, 4, 0, 0]} />
                        <Bar yAxisId="right" dataKey="co2eKg" fill="#8b5cf6" name="CO₂e (kg)" radius={[4, 4, 0, 0]} />
                    </BarChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
}

export default Dashboard;
