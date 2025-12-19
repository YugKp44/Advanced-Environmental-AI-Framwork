import { useState, useEffect } from 'react';
import {
    LineChart, Line, AreaChart, Area, BarChart, Bar,
    XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import { getHistoricalTrends, getForecast, getDepartmentComparison, getYearOverYear } from '../services/api';

function Analytics({ company }) {
    const [trends, setTrends] = useState([]);
    const [forecasts, setForecasts] = useState([]);
    const [comparison, setComparison] = useState([]);
    const [yoy, setYoy] = useState({});
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('trends');

    useEffect(() => {
        loadData();
    }, [company.id]);

    const loadData = async () => {
        try {
            setLoading(true);
            const [trendsData, forecastData, comparisonData, yoyData] = await Promise.all([
                getHistoricalTrends(company.id, 6),
                getForecast(company.id, 6),
                getDepartmentComparison(company.id),
                getYearOverYear(company.id)
            ]);
            setTrends(trendsData);
            setForecasts(forecastData);
            setComparison(comparisonData);
            setYoy(yoyData);
        } catch (error) {
            console.error('Error loading analytics:', error);
        } finally {
            setLoading(false);
        }
    };

    // Combine historical and forecast data
    const combinedData = [
        ...trends.map(t => ({ ...t, type: 'actual' })),
        ...forecasts.map(f => ({
            period: f.period,
            aiEnergyKwh: f.predictedAiKwh,
            co2eKg: f.predictedCo2eKg,
            cost: f.predictedCost,
            confidenceLow: f.confidenceLow,
            confidenceHigh: f.confidenceHigh,
            type: 'forecast'
        }))
    ];

    if (loading) {
        return <div className="loading"><div className="spinner"></div></div>;
    }

    return (
        <div className="analytics">
            <div className="page-header">
                <h1 className="page-title">📈 Analytics & Forecasting</h1>
                <p className="page-subtitle">Historical trends, comparisons, and predictions</p>
            </div>

            {/* Tabs */}
            <div className="tabs">
                <button className={`tab ${activeTab === 'trends' ? 'active' : ''}`} onClick={() => setActiveTab('trends')}>
                    📊 Trends & Forecast
                </button>
                <button className={`tab ${activeTab === 'comparison' ? 'active' : ''}`} onClick={() => setActiveTab('comparison')}>
                    🏢 Department Comparison
                </button>
                <button className={`tab ${activeTab === 'yoy' ? 'active' : ''}`} onClick={() => setActiveTab('yoy')}>
                    📅 Year-over-Year
                </button>
            </div>

            {/* Trends & Forecast */}
            {activeTab === 'trends' && (
                <div>
                    <div className="chart-card" style={{ marginBottom: '1.5rem' }}>
                        <h3 className="chart-title">AI Energy Usage - Historical + Forecast</h3>
                        <ResponsiveContainer width="100%" height={400}>
                            <AreaChart data={combinedData}>
                                <defs>
                                    <linearGradient id="colorActual" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#10b981" stopOpacity={0.3} />
                                        <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                                    </linearGradient>
                                    <linearGradient id="colorForecast" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.3} />
                                        <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0} />
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
                                <Area
                                    type="monotone"
                                    dataKey="aiEnergyKwh"
                                    stroke="#10b981"
                                    fill="url(#colorActual)"
                                    name="AI Energy (kWh)"
                                    strokeWidth={2}
                                />
                                {forecasts.length > 0 && (
                                    <Area
                                        type="monotone"
                                        dataKey="confidenceHigh"
                                        stroke="#8b5cf6"
                                        fill="url(#colorForecast)"
                                        name="Forecast Range"
                                        strokeDasharray="5 5"
                                        opacity={0.5}
                                    />
                                )}
                            </AreaChart>
                        </ResponsiveContainer>
                        <div style={{ display: 'flex', gap: '2rem', marginTop: '1rem', justifyContent: 'center' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                <div style={{ width: '20px', height: '3px', background: '#10b981' }}></div>
                                <span style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>Actual Data</span>
                            </div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                <div style={{ width: '20px', height: '3px', background: '#8b5cf6', borderStyle: 'dashed' }}></div>
                                <span style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>Forecast (±15% confidence)</span>
                            </div>
                        </div>
                    </div>

                    <div className="charts-grid">
                        <div className="chart-card">
                            <h3 className="chart-title">Carbon Emissions Trend</h3>
                            <ResponsiveContainer width="100%" height={300}>
                                <LineChart data={trends}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                                    <XAxis dataKey="period" stroke="#6b7280" fontSize={12} />
                                    <YAxis stroke="#6b7280" fontSize={12} />
                                    <Tooltip contentStyle={{ background: '#1a2332', border: '1px solid #374151', borderRadius: '8px' }} />
                                    <Line type="monotone" dataKey="co2eKg" stroke="#ec4899" strokeWidth={2} name="CO₂e (kg)" dot={{ fill: '#ec4899' }} />
                                </LineChart>
                            </ResponsiveContainer>
                        </div>

                        <div className="chart-card">
                            <h3 className="chart-title">Monthly Cost Trend</h3>
                            <ResponsiveContainer width="100%" height={300}>
                                <BarChart data={trends}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                                    <XAxis dataKey="period" stroke="#6b7280" fontSize={12} />
                                    <YAxis stroke="#6b7280" fontSize={12} />
                                    <Tooltip contentStyle={{ background: '#1a2332', border: '1px solid #374151', borderRadius: '8px' }} />
                                    <Bar dataKey="cost" fill="#f59e0b" name="Cost ($)" radius={[4, 4, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                </div>
            )}

            {/* Department Comparison */}
            {activeTab === 'comparison' && (
                <div className="chart-card">
                    <h3 className="chart-title">Department AI Energy Comparison</h3>
                    <ResponsiveContainer width="100%" height={400}>
                        <BarChart data={comparison} layout="vertical">
                            <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                            <XAxis type="number" stroke="#6b7280" fontSize={12} />
                            <YAxis type="category" dataKey="departmentName" stroke="#6b7280" fontSize={12} width={150} />
                            <Tooltip contentStyle={{ background: '#1a2332', border: '1px solid #374151', borderRadius: '8px' }} />
                            <Legend />
                            <Bar dataKey="aiEnergyKwh" fill="#10b981" name="AI Energy (kWh)" radius={[0, 4, 4, 0]} />
                        </BarChart>
                    </ResponsiveContainer>

                    <div style={{ marginTop: '2rem' }}>
                        <h4 style={{ marginBottom: '1rem' }}>Department Details</h4>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Department</th>
                                    <th>AI Usage Weight</th>
                                    <th>AI Energy (kWh)</th>
                                    <th>% of Total</th>
                                </tr>
                            </thead>
                            <tbody>
                                {comparison.map((dept, index) => (
                                    <tr key={index}>
                                        <td>{dept.departmentName}</td>
                                        <td>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                                <div style={{
                                                    width: '60px',
                                                    height: '8px',
                                                    background: 'var(--color-surface-elevated)',
                                                    borderRadius: '4px',
                                                    overflow: 'hidden'
                                                }}>
                                                    <div style={{
                                                        width: `${(dept.aiUsageWeight || 0) * 100}%`,
                                                        height: '100%',
                                                        background: 'var(--color-primary)'
                                                    }}></div>
                                                </div>
                                                <span>{((dept.aiUsageWeight || 0) * 100).toFixed(0)}%</span>
                                            </div>
                                        </td>
                                        <td style={{ fontFamily: 'var(--font-mono)' }}>{dept.aiEnergyKwh?.toFixed(2)}</td>
                                        <td><span className="badge badge-success">{dept.percentage?.toFixed(1)}%</span></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* Year-over-Year */}
            {activeTab === 'yoy' && (
                <div className="chart-card">
                    <h3 className="chart-title">Year-over-Year Comparison</h3>
                    <p style={{ color: 'var(--color-text-muted)', marginBottom: '1.5rem' }}>{yoy.period}</p>

                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1.5rem' }}>
                        <div style={{ padding: '1.5rem', background: 'var(--color-surface-elevated)', borderRadius: 'var(--radius-lg)' }}>
                            <div style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '0.5rem' }}>This Year AI Energy</div>
                            <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--color-primary)' }}>
                                {(yoy.thisYearAiKwh || 0).toLocaleString()} kWh
                            </div>
                            <div className={`kpi-change ${(yoy.aiKwhChangePercent || 0) >= 0 ? 'negative' : 'positive'}`} style={{ marginTop: '0.5rem' }}>
                                {(yoy.aiKwhChangePercent || 0) >= 0 ? '↑' : '↓'} {Math.abs(yoy.aiKwhChangePercent || 0).toFixed(1)}% vs last year
                            </div>
                        </div>

                        <div style={{ padding: '1.5rem', background: 'var(--color-surface-elevated)', borderRadius: 'var(--radius-lg)' }}>
                            <div style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '0.5rem' }}>Last Year AI Energy</div>
                            <div style={{ fontSize: '2rem', fontWeight: 700 }}>
                                {(yoy.lastYearAiKwh || 0).toLocaleString()} kWh
                            </div>
                        </div>

                        <div style={{ padding: '1.5rem', background: 'var(--color-surface-elevated)', borderRadius: 'var(--radius-lg)' }}>
                            <div style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '0.5rem' }}>This Year Total Energy</div>
                            <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--color-accent-blue)' }}>
                                {(yoy.thisYearTotalKwh || 0).toLocaleString()} kWh
                            </div>
                            <div className={`kpi-change ${(yoy.totalKwhChangePercent || 0) >= 0 ? 'negative' : 'positive'}`} style={{ marginTop: '0.5rem' }}>
                                {(yoy.totalKwhChangePercent || 0) >= 0 ? '↑' : '↓'} {Math.abs(yoy.totalKwhChangePercent || 0).toFixed(1)}% vs last year
                            </div>
                        </div>

                        <div style={{ padding: '1.5rem', background: 'var(--color-surface-elevated)', borderRadius: 'var(--radius-lg)' }}>
                            <div style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginBottom: '0.5rem' }}>Last Year Total Energy</div>
                            <div style={{ fontSize: '2rem', fontWeight: 700 }}>
                                {(yoy.lastYearTotalKwh || 0).toLocaleString()} kWh
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Analytics;
