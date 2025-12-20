import { useState, useEffect } from 'react';

const warmupStages = [
    { icon: "🖥️", message: "Waking up server...", detail: "Free tier servers sleep after 15 minutes of inactivity" },
    { icon: "🗄️", message: "Initializing database...", detail: "Loading H2 in-memory database" },
    { icon: "🌱", message: "Starting Spring Boot...", detail: "Compiling and loading application context" },
    { icon: "🔋", message: "Loading energy modules...", detail: "Preparing energy tracking and carbon calculation engines" },
    { icon: "📊", message: "Generating sample data...", detail: "Creating demo datasets for demonstration" },
    { icon: "✅", message: "Almost ready...", detail: "Finalizing API endpoints" },
];

const tips = [
    "💡 Running on Render's free tier - server spins down after inactivity",
    "☕ Grab a coffee - cold starts can take 1-2 minutes",
    "🌍 Track your organization's carbon footprint in real-time",
    "⚡ Monitor energy consumption across all departments",
    "🎯 AI-powered insights help optimize energy efficiency",
    "📉 Reduce costs and environmental impact together",
    "🔄 The server is spinning up - thanks for your patience!",
    "🌱 Once loaded, the app will be fast until the next idle timeout",
];

function ServerWarmup({ onReady, error, retryCount }) {
    const [currentStage, setCurrentStage] = useState(0);
    const [currentTip, setCurrentTip] = useState(0);
    const [dots, setDots] = useState('');

    useEffect(() => {
        // Cycle through stages every 5 seconds
        const stageInterval = setInterval(() => {
            setCurrentStage(prev => (prev + 1) % warmupStages.length);
        }, 5000);

        // Cycle tips every 4 seconds
        const tipInterval = setInterval(() => {
            setCurrentTip(prev => (prev + 1) % tips.length);
        }, 4000);

        // Animate loading dots
        const dotsInterval = setInterval(() => {
            setDots(prev => prev.length >= 3 ? '' : prev + '.');
        }, 500);

        return () => {
            clearInterval(stageInterval);
            clearInterval(tipInterval);
            clearInterval(dotsInterval);
        };
    }, []);

    const stage = warmupStages[currentStage];

    return (
        <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            width: '100vw',
            height: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: 'linear-gradient(135deg, #0a0f1a 0%, #030712 50%, #0d1117 100%)',
            padding: '2rem',
            overflow: 'hidden',
            zIndex: 9999
        }}>
            {/* Animated background orbs - Green eco theme */}
            <div style={{
                position: 'absolute',
                top: '10%',
                left: '20%',
                width: '400px',
                height: '400px',
                background: 'radial-gradient(circle, rgba(16, 185, 129, 0.15) 0%, transparent 70%)',
                borderRadius: '50%',
                filter: 'blur(60px)',
                animation: 'pulse 4s ease-in-out infinite'
            }}></div>
            <div style={{
                position: 'absolute',
                bottom: '20%',
                right: '15%',
                width: '300px',
                height: '300px',
                background: 'radial-gradient(circle, rgba(52, 211, 153, 0.12) 0%, transparent 70%)',
                borderRadius: '50%',
                filter: 'blur(60px)',
                animation: 'pulse 5s ease-in-out infinite reverse'
            }}></div>
            <div style={{
                position: 'absolute',
                top: '60%',
                left: '10%',
                width: '250px',
                height: '250px',
                background: 'radial-gradient(circle, rgba(5, 150, 105, 0.1) 0%, transparent 70%)',
                borderRadius: '50%',
                filter: 'blur(60px)',
                animation: 'pulse 6s ease-in-out infinite'
            }}></div>

            <div style={{
                background: 'rgba(17, 24, 39, 0.85)',
                backdropFilter: 'blur(20px)',
                border: '1px solid rgba(16, 185, 129, 0.15)',
                borderRadius: '24px',
                padding: '3rem',
                maxWidth: '520px',
                width: '100%',
                textAlign: 'center',
                position: 'relative',
                boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5), 0 0 40px rgba(16, 185, 129, 0.1)'
            }}>
                {/* Logo */}
                <div style={{
                    width: '80px',
                    height: '80px',
                    background: 'linear-gradient(135deg, #10b981 0%, #059669 50%, #047857 100%)',
                    borderRadius: '20px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    margin: '0 auto 1.5rem',
                    boxShadow: '0 10px 40px rgba(16, 185, 129, 0.4)',
                    fontSize: '2.5rem'
                }}>
                    🌱
                </div>

                <h1 style={{
                    fontSize: '1.75rem',
                    fontWeight: '800',
                    marginBottom: '0.5rem',
                    background: 'linear-gradient(135deg, #f9fafb 0%, #34d399 100%)',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                    backgroundClip: 'text'
                }}>
                    EcoAI Framework
                </h1>

                <p style={{
                    color: '#9ca3af',
                    marginBottom: '2rem',
                    fontSize: '0.95rem'
                }}>
                    Energy & Carbon Management
                </p>

                <p style={{
                    color: '#6b7280',
                    marginBottom: '1.5rem',
                    fontSize: '0.9rem'
                }}>
                    Starting up the server{dots}
                </p>

                {/* Progress indicator */}
                <div style={{
                    display: 'flex',
                    justifyContent: 'center',
                    gap: '8px',
                    marginBottom: '2rem'
                }}>
                    {warmupStages.map((_, idx) => (
                        <div
                            key={idx}
                            style={{
                                width: idx === currentStage ? '24px' : '8px',
                                height: '8px',
                                borderRadius: '4px',
                                background: idx <= currentStage
                                    ? 'linear-gradient(90deg, #10b981, #34d399)'
                                    : 'rgba(255, 255, 255, 0.1)',
                                transition: 'all 0.3s ease'
                            }}
                        />
                    ))}
                </div>

                {/* Current stage */}
                <div style={{
                    background: 'rgba(16, 185, 129, 0.1)',
                    border: '1px solid rgba(16, 185, 129, 0.2)',
                    borderRadius: '16px',
                    padding: '1.5rem',
                    marginBottom: '1.5rem'
                }}>
                    <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '12px',
                        marginBottom: '8px'
                    }}>
                        <div style={{
                            animation: 'spin 2s linear infinite',
                            fontSize: '1.5rem'
                        }}>
                            {stage.icon}
                        </div>
                        <span style={{
                            fontSize: '1.1rem',
                            fontWeight: '600',
                            color: '#f9fafb'
                        }}>
                            {stage.message}
                        </span>
                    </div>
                    <p style={{
                        fontSize: '0.85rem',
                        color: '#9ca3af',
                        margin: 0
                    }}>
                        {stage.detail}
                    </p>
                </div>

                {/* Server spinning up info - shown after first retry */}
                {retryCount > 0 && (
                    <div style={{
                        background: 'rgba(16, 185, 129, 0.1)',
                        border: '1px solid rgba(16, 185, 129, 0.2)',
                        borderRadius: '12px',
                        padding: '12px',
                        marginBottom: '1.5rem',
                        fontSize: '0.85rem',
                        color: '#10b981',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '8px'
                    }}>
                        <span style={{ animation: 'spin 1s linear infinite' }}>🔄</span>
                        Server is spinning up on Render free tier... ({retryCount} checks)
                    </div>
                )}

                {/* Rotating tips */}
                <div style={{
                    padding: '1rem',
                    background: 'rgba(255, 255, 255, 0.03)',
                    borderRadius: '12px',
                    minHeight: '60px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                }}>
                    <p style={{
                        fontSize: '0.9rem',
                        color: '#6b7280',
                        margin: 0,
                        transition: 'opacity 0.3s ease'
                    }}>
                        {tips[currentTip]}
                    </p>
                </div>

                {/* Estimated time */}
                <p style={{
                    marginTop: '1.5rem',
                    fontSize: '0.8rem',
                    color: '#4b5563'
                }}>
                    Using Render free tier - cold starts may take 1-2 minutes
                </p>

                {/* Eco stats decoration */}
                <div style={{
                    display: 'flex',
                    justifyContent: 'center',
                    gap: '2rem',
                    marginTop: '1.5rem',
                    paddingTop: '1.5rem',
                    borderTop: '1px solid rgba(255, 255, 255, 0.05)'
                }}>
                    <div style={{ textAlign: 'center' }}>
                        <div style={{ fontSize: '1.25rem', marginBottom: '4px' }}>⚡</div>
                        <div style={{ fontSize: '0.7rem', color: '#4b5563' }}>Energy</div>
                    </div>
                    <div style={{ textAlign: 'center' }}>
                        <div style={{ fontSize: '1.25rem', marginBottom: '4px' }}>🌿</div>
                        <div style={{ fontSize: '0.7rem', color: '#4b5563' }}>Carbon</div>
                    </div>
                    <div style={{ textAlign: 'center' }}>
                        <div style={{ fontSize: '1.25rem', marginBottom: '4px' }}>📊</div>
                        <div style={{ fontSize: '0.7rem', color: '#4b5563' }}>Analytics</div>
                    </div>
                    <div style={{ textAlign: 'center' }}>
                        <div style={{ fontSize: '1.25rem', marginBottom: '4px' }}>🤖</div>
                        <div style={{ fontSize: '0.7rem', color: '#4b5563' }}>AI Insights</div>
                    </div>
                </div>
            </div>

            <style>{`
                @keyframes pulse {
                    0%, 100% { transform: scale(1); opacity: 0.5; }
                    50% { transform: scale(1.1); opacity: 0.8; }
                }
                @keyframes spin {
                    from { transform: rotate(0deg); }
                    to { transform: rotate(360deg); }
                }
            `}</style>
        </div>
    );
}

export default ServerWarmup;
