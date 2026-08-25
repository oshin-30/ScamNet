import { useEffect, useState } from 'react';

function ClusterView({ onClose }) {
  const [clusters, setClusters] = useState([]);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    fetch('http://localhost:8080/api/posts/clusters')
      .then(res => res.json())
      .then(data => setClusters(data));
  }, []);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="cluster-modal" onClick={(e) => e.stopPropagation()}>
        <div className="cluster-header">
          <h2>Suspicious Networks</h2>
            <div className="filter-tabs">
            <button className={filter === 'ALL' ? 'tab-active' : ''} onClick={() => setFilter('ALL')}>All</button>
            <button className={filter === 'MARKETPLACE' ? 'tab-active' : ''} onClick={() => setFilter('MARKETPLACE')}>Marketplace</button>
            <button className={filter === 'JOB' ? 'tab-active' : ''} onClick={() => setFilter('JOB')}>Jobs</button>
          </div>
          <button className="secondary-btn" onClick={onClose}>Close</button>
        </div>

        {clusters.length === 0 ? (
          <div className="empty">No connected fraud networks detected yet.</div>
        ) : (
            clusters
            .filter(c => filter === 'ALL' || c.posts.every(p => p.type === filter))
            .map(cluster => (
            <div key={cluster.clusterId} className="cluster-card">
              <div className="cluster-title">
                <span>Network #{String(cluster.clusterId).padStart(2, '0')}</span>
                <span className={`cluster-risk ${cluster.riskLevel >= 0.8 ? 'high' : cluster.riskLevel > 0 ? 'medium' : ''}`}>
                  Risk: {Math.round(cluster.riskLevel * 100)}%
                </span>
              </div>

              <div className="cluster-stats">
                <span>{cluster.postCount} listings</span>
                <span>{cluster.accountCount} accounts</span>
                {cluster.sharedContacts && <span>shared contact</span>}
              </div>

              <div className="cluster-graph">
                {cluster.posts.map(post => (
                  <div key={post.id} className="graph-node">
                    <img src={`http://localhost:8080/uploads/${post.imageUrl}`} alt={post.title} />
                    <p>{post.title}</p>
                    <p className="node-meta">{post.posterName}</p>
                  </div>
                ))}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default ClusterView;