import { useState } from 'react';
import { simulateGrowth, simulateRegionChange, simulateEfficiency, getDefaultCarbonIntensities } from '../services/api';

function Simulation({ company }) {
    const [simulationType, setSimulationType] = useState('growth');
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState(null);

    // Form states
    const [growthPercent, setGrowthPercent] = useState(20);
    const [monthsAhead, setMonthsAhead] = useState(12);
    const [fromRegion, setFromRegion] = useState('IN');
    const [toRegion, setToRegion] = useState('EU');
    const [efficiencyPercent, setEfficiencyPercent] = useState(15);

    const runSimulation = async () => {
        setLoading(true);
        setResult(null);

        try {
            let data;
            switch (simulationType) {
                case 'growth':
                    data = await simulateGrowth(company.id, growthPercent, monthsAhead);
                    break;
                case 'region':
                    data = await simulateRegionChange(company.id, fromRegion, toRegion);
                    break;
                case 'efficiency':
                    data = await simulateEfficiency(company.id, efficiencyPercent);
                    break;
            }
            setResult(data);
        } catch (error) {
            console.error('Simulation error:', error);
        } finally {
            setLoading(false);
        }
    };

    const formatNumber = (num) => {
        if (num === null || num === undefined) return '0';
        return num.toLocaleString('en-US', { maximumFractionDigits: 2 });
    };

    const regions = [
        { code: 'IN', name: 'India (708 gCO₂/kWh)' },
        { code: 'US', name: 'USA (386 gCO₂/kWh)' },
        { code: 'EU', name: 'EU Average (276 gCO₂/kWh)' },
        { code: 'UK', name: 'UK (233 gCO₂/kWh)' },
        { code: 'CA', name: 'Canada (120 gCO₂/kWh)' },
        { code: 'NO', name: 'Norway (26 gCO₂/kWh)' },
    ];

    return (
        <div className="simulation">
            <div className="page-header">
                <h1 className="page-title">🔮 What-If Simulator</h1>
                <p className="page-subtitle">Explore scenarios and plan for the future</p>
            </div>

            {/* Simulation Type Tabs */}
            <div className="tabs">
                <button
                    className={`tab ${simulationType === 'growth' ? 'active' : ''}`}
                    onClick={() => { setSimulationType('growth'); setResult(null); }}
                >
                    📈 Growth Scenario
                </button>
                <button
                    className={`tab ${simulationType === 'region' ? 'active' : ''}`}
                    onClick={() => { setSimulationType('region'); setResult(null); }}
                >
                    🌍 Region Change
                </button>
                <button
                    className={`tab ${simulationType === 'efficiency' ? 'active' : ''}`}
                    onClick={() => { setSimulationType('efficiency'); setResult(null); }}
                >
                    ⚡ Efficiency Gain
                </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.5rem' }}>
                {/* Parameters Card */}
                <div className="card">
                    <h3 className="chart-title">Simulation Parameters</h3>

                    {simulationType === 'growth' && (
                        <>
                            <div className="form-group">
                                <label className="form-label">Growth Rate (%)</label>
                                <input
                                    type="range"
                                    min="5"
                                    max="100"
                                    value={growthPercent}
                                    onChange={(e) => setGrowthPercent(parseInt(e.target.value))}
                                    style={{ width: '100%' }}
                                />
                                <div style={{ textAlign: 'center', fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-primary)' }}>
                                    +{growthPercent}%
                                </div>
                            </div>
                            <div className="form-group">
                                <label className="form-label">Projection Period (months)</label>
                                <select
                                    className="form-select"
                                    value={monthsAhead}
                                    onChange={(e) => setMonthsAhead(parseInt(e.target.value))}
                                >
                                    <option value={3}>3 months</option>
                                    <option value={6}>6 months</option>
                                    <option value={12}>12 months</option>
                                    <option value={24}>24 months</option>
                                </select>
                            </div>
                            <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginTop: '1rem' }}>
                                "What if AI usage grows by {growthPercent}% over the next {monthsAhead} months?"
                            </p>
                        </>
                    )}

                    {simulationType === 'region' && (
                        <>
                            <div className="form-group">
                                <label className="form-label">From Region</label>
                                <select
                                    className="form-select"
                                    value={fromRegion}
                                    onChange={(e) => setFromRegion(e.target.value)}
                                >
                                    {regions.map(r => <option key={r.code} value={r.code}>{r.name}</option>)}
                                </select>
                            </div>
                            <div className="form-group">
                                <label className="form-label">To Region</label>
                                <select
                                    className="form-select"
                                    value={toRegion}
                                    onChange={(e) => setToRegion(e.target.value)}
                                >
                                    {regions.map(r => <option key={r.code} value={r.code}>{r.name}</option>)}
                                </select>
                            </div>
                            <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginTop: '1rem' }}>
                                "What if we move AI workloads from {fromRegion} to {toRegion}?"
                            </p>
                        </>
                    )}

                    {simulationType === 'efficiency' && (
                        <>
                            <div className="form-group">
                                <label className="form-label">Efficiency Improvement (%)</label>
                                <input
                                    type="range"
                                    min="5"
                                    max="50"
                                    value={efficiencyPercent}
                                    onChange={(e) => setEfficiencyPercent(parseInt(e.target.value))}
                                    style={{ width: '100%' }}
                                />
                                <div style={{ textAlign: 'center', fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-success)' }}>
                                    -{efficiencyPercent}% energy
                                </div>
                            </div>
                            <p style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)', marginTop: '1rem' }}>
                                "What if we improve AI efficiency by {efficiencyPercent}%?"
                            </p>
                        </>
                    )}

                    <button
                        className="btn btn-primary"
                        onClick={runSimulation}
                        disabled={loading}
                        style={{ width: '100%', marginTop: '1rem' }}
                    >
                        {loading ? '⏳ Running Simulation...' : '▶️ Run Simulation'}
                    </button>
                </div>

                {/* Results Card */}
                <div className="card">
                    <h3 className="chart-title">Simulation Results</h3>

                    {!result && !loading && (
                        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--color-text-muted)' }}>
                            <p style={{ fontSize: '3rem', marginBottom: '1rem' }}>🔮</p>
                            <p>Configure parameters and run the simulation to see projected impacts.</p>
                        </div>
                    )}

                    {loading && (
                        <div className="loading">
                            <div className="spinner"></div>
                        </div>
                    )}

                    {result && (
                        <>
                            <p style={{ marginBottom: '1rem', color: 'var(--color-text-secondary)' }}>
                                {result.description}
                            </p>

                            <div className="simulation-results">
                                <div className="simulation-metric">
                                    <div className="label">Baseline AI Energy</div>
                                    <div className="value">{formatNumber(result.baselineAiKwh)} kWh</div>
                                </div>
                                <div className="simulation-metric">
                                    <div className="label">→</div>
                                    <div className="value" style={{ fontSize: '1.5rem' }}>→</div>
                                </div>
                                <div className="simulation-metric">
                                    <div className="label">Projected AI Energy</div>
                                    <div className="value" style={{ color: 'var(--color-primary)' }}>{formatNumber(result.projectedAiKwh)} kWh</div>
                                    <div className={`delta ${result.energyDeltaKwh >= 0 ? 'positive' : 'negative'}`}>
                                        {result.energyDeltaKwh >= 0 ? '+' : ''}{formatNumber(result.energyDeltaKwh)} kWh
                                    </div>
                                </div>
                            </div>

                            <div className="simulation-results">
                                <div className="simulation-metric">
                                    <div className="label">Baseline CO₂e</div>
                                    <div className="value">{formatNumber(result.baselineCo2eKg)} kg</div>
                                </div>
                                <div className="simulation-metric">
                                    <div className="label">→</div>
                                    <div className="value" style={{ fontSize: '1.5rem' }}>→</div>
                                </div>
                                <div className="simulation-metric">
                                    <div className="label">Projected CO₂e</div>
                                    <div className="value" style={{ color: result.carbonDeltaKg <= 0 ? 'var(--color-success)' : 'var(--color-error)' }}>
                                        {formatNumber(result.projectedCo2eKg)} kg
                                    </div>
                                    <div className={`delta ${result.carbonDeltaKg >= 0 ? 'positive' : 'negative'}`}>
                                        {result.carbonDeltaKg >= 0 ? '+' : ''}{formatNumber(result.carbonDeltaKg)} kg
                                    </div>
                                </div>
                            </div>

                            <div className="simulation-results">
                                <div className="simulation-metric">
                                    <div className="label">Baseline Cost</div>
                                    <div className="value">${formatNumber(result.baselineCost)}</div>
                                </div>
                                <div className="simulation-metric">
                                    <div className="label">→</div>
                                    <div className="value" style={{ fontSize: '1.5rem' }}>→</div>
                                </div>
                                <div className="simulation-metric">
                                    <div className="label">Projected Cost</div>
                                    <div className="value" style={{ color: result.costDelta <= 0 ? 'var(--color-success)' : 'var(--color-warning)' }}>
                                        ${formatNumber(result.projectedCost)}
                                    </div>
                                    <div className={`delta ${result.costDelta >= 0 ? 'positive' : 'negative'}`}>
                                        {result.costDelta >= 0 ? '+' : ''} ${formatNumber(result.costDelta)}
                                    </div>
                                </div>
                            </div>

                            <div style={{
                                marginTop: '1.5rem',
                                padding: '1rem',
                                background: 'var(--color-primary-glow)',
                                borderRadius: 'var(--radius-md)',
                                textAlign: 'center'
                            }}>
                                <div style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>Overall Impact</div>
                                <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--color-primary)' }}>
                                    {result.percentChange >= 0 ? '+' : ''}{result.percentChange?.toFixed(1)}%
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}

export default Simulation;
