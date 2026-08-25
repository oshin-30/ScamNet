import { useEffect, useState } from 'react';
import './App.css';
import ClusterView from './ClusterView';

function App() {
  const [posts, setPosts] = useState([]);
    const [showForm, setShowForm] = useState(false);
    const [showClusters, setShowClusters] = useState(false);
    const [clusterCount, setClusterCount] = useState(0);
  const fetchClusterCount = () => {
    fetch('http://localhost:8080/api/posts/clusters')
      .then(res => res.json())
      .then(data => setClusterCount(data.length));
  };
  const [formData, setFormData] = useState({
    type: 'MARKETPLACE', title: '', description: '',
    posterName: '', posterContact: '', location: ''
  });
  const [imageFile, setImageFile] = useState(null);
  const [submitting, setSubmitting] = useState(false);

    const fetchPosts = () => {
    fetch('http://localhost:8080/api/posts')
      .then(res => res.json())
      .then(data => setPosts(data));
    fetchClusterCount();
  };
  const [theme, setTheme] = useState('dark');

      useEffect(() => {
    fetchPosts();
    fetchClusterCount();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!imageFile) { alert('Please select an image'); return; }
    setSubmitting(true);

    const data = new FormData();
    Object.entries(formData).forEach(([key, val]) => data.append(key, val));
    data.append('image', imageFile);

    try {
      const res = await fetch('http://localhost:8080/api/posts', {
        method: 'POST',
        body: data
      });
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(res.status === 413 ? 'Image too large (max 10MB)' : 'Submission failed — try again');
      }
      await fetchPosts();
      setShowForm(false);
      setFormData({ type: 'MARKETPLACE', title: '', description: '', posterName: '', posterContact: '', location: '' });
      setImageFile(null);
    } catch (err) {
      alert('Error submitting post: ' + err.message);
    } finally {
      setSubmitting(false);
    }
  };
    const handleDelete = async (id) => {
    if (!window.confirm('Delete this post?')) return;
    await fetch(`http://localhost:8080/api/posts/${id}`, { method: 'DELETE' });
    fetchPosts();
  };

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark');
  };

  const flagged = posts.filter(p => (p.riskScore || 0) > 0).length;
  const high = posts.filter(p => (p.riskScore || 0) >= 0.8).length;

  return (
    <div data-theme={theme}>
      <nav>
        <div className="logo">Scam<span>Net</span></div>
        <div className="nav-right">
          <div className="live"><span className="dot"></span> LIVE FEED</div>
          <button className="theme-btn" onClick={toggleTheme}>
            {theme === 'dark' ? '☀ Light' : '🌙 Dark'}
          </button>
        </div>
      </nav>
      <div className="hero">
        <h1>Catch scam rings before they scale.</h1>
        <p>ScamNet cross-references images and contact details across every listing to surface coordinated fraud — not just single bad posts.</p>
      </div>
      <div className="hero-actions">
        <button className="primary-btn" onClick={() => setShowForm(true)}>+ New Listing</button>
        <button className="secondary-btn" onClick={() => setShowClusters(true)}>View Fraud Networks</button>
      </div>

      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <form className="modal" onClick={(e) => e.stopPropagation()} onSubmit={handleSubmit}>
            <h2>New Listing</h2>

            <select value={formData.type} onChange={(e) => setFormData({...formData, type: e.target.value})}>
              <option value="MARKETPLACE">Marketplace Item</option>
              <option value="JOB">Job Posting</option>
            </select>

            <input placeholder="Title" required
              value={formData.title}
              onChange={(e) => setFormData({...formData, title: e.target.value})} />

            <textarea placeholder="Description" required
              value={formData.description}
              onChange={(e) => setFormData({...formData, description: e.target.value})} />

            <input placeholder="Your name" required
              value={formData.posterName}
              onChange={(e) => setFormData({...formData, posterName: e.target.value})} />

            <input placeholder="Contact number" required
              value={formData.posterContact}
              onChange={(e) => setFormData({...formData, posterContact: e.target.value})} />

            <input placeholder="Location" required
              value={formData.location}
              onChange={(e) => setFormData({...formData, location: e.target.value})} />

            <input type="file" accept="image/*" required
              onChange={(e) => setImageFile(e.target.files[0])} />

            <div className="modal-actions">
              <button type="button" className="secondary-btn" onClick={() => setShowForm(false)}>Cancel</button>
              <button type="submit" className="primary-btn" disabled={submitting}>
                {submitting ? 'Checking for fraud...' : 'Submit Listing'}
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="stats">
        <div className="stat">
          <div className="num">{posts.length}</div>
          <div className="label">Total Posts</div>
        </div>
        <div className="stat">
          <div className="num">{flagged}</div>
          <div className="label">Flagged</div>
        </div>
        <div className="stat">
          <div className="num">{high}</div>
          <div className="label">High Risk</div>
        </div>
        <div className="stat stat-clickable" onClick={() => setShowClusters(true)}>
          <div className="num">{clusterCount}</div>
          <div className="label">Suspicious Networks</div>
        </div>
      </div>

      <main>
        {posts.length === 0 ? (
          <div className="empty">No posts yet.</div>
        ) : (
          <div className="grid">
            {posts.slice().reverse().map(post => {
              const score = post.riskScore || 0;
              let riskClass = '', badgeClass = 'badge-low', label = 'CLEAR';
              if (score >= 0.8) { riskClass = 'risk-high'; badgeClass = 'badge-high'; label = 'HIGH RISK'; }
              else if (score > 0) { riskClass = 'risk-medium'; badgeClass = 'badge-medium'; label = 'SUSPICIOUS'; }

              return (
                  <div key={post.id} className="post">
                  <button className="delete-btn" onClick={() => handleDelete(post.id)}>×</button>
                  <div className="post-top">
                  <div className={`gauge gauge-${score >= 0.8 ? 'high' : score > 0 ? 'medium' : 'low'}`}>
                  {score.toFixed(1)}
                  </div>
                  <span className={`status ${score >= 0.8 ? 'status-high' : score > 0 ? 'status-medium' : ''}`}>{label}</span>
                  <span className={`type-tag ${post.type === 'JOB' ? 'type-job' : 'type-market'}`}>
                    {post.type === 'JOB' ? 'JOB' : 'MARKET'}
                  </span>
                  </div>
                  <h3>{post.title}</h3>
                  <p>{post.description}</p>
                  <p className="location-line">📍 {post.location}</p>
                  <p>{post.posterName} · {post.posterContact}</p>
                  <img src={`http://localhost:8080/uploads/${post.imageUrl}`} alt={post.title} />
                    {post.riskReasons && (
                    <div className="reasons">
                      {post.riskReasons.split(' | ').map((r, i) => (
                        <p key={i}>⚠ {r}</p>
                      ))}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </main>
      {showClusters && <ClusterView onClose={() => setShowClusters(false)} />}
    </div>
  );
}

export default App;