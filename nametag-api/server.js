const express = require('express');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// In-memory cache for nametags
// Key: UUID, Value: { text, formatting, lastUpdate }
const tagsCache = new Map();

// Time to live (TTL) in milliseconds. Default: 5 minutes
// If a player doesn't ping the server within this time, their tag drops
const TTL = 5 * 60 * 1000; 

app.post('/api/nametags/update', (req, res) => {
    const { uuid, text, formatting } = req.body;
    
    if (!uuid || typeof uuid !== 'string') {
        return res.status(400).json({ error: 'Missing or invalid uuid' });
    }
    
    if (text && typeof text === 'string') {
        tagsCache.set(uuid, {
            text: text.substring(0, 64), // limit length
            formatting: formatting || {},
            lastUpdate: Date.now()
        });
    } else {
        // If text is empty or missing, delete the tag
        tagsCache.delete(uuid);
    }
    
    res.json({ success: true });
});

app.get('/api/nametags', (req, res) => {
    const now = Date.now();
    const result = {};
    
    for (const [uuid, data] of tagsCache.entries()) {
        if (now - data.lastUpdate > TTL) {
            tagsCache.delete(uuid);
        } else {
            result[uuid] = {
                text: data.text,
                formatting: data.formatting
            };
        }
    }
    
    res.json(result);
});

// Periodic cleanup task every minute
setInterval(() => {
    const now = Date.now();
    for (const [uuid, data] of tagsCache.entries()) {
        if (now - data.lastUpdate > TTL) {
            tagsCache.delete(uuid);
        }
    }
}, 60 * 1000);

app.listen(PORT, () => {
    console.log(`Nametag API running on port ${PORT}`);
});
