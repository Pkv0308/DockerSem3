const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

const app = express();
const PORT = 3000;

// Middleware
app.use(express.json()); // Allows us to parse incoming JSON bodies
app.use(cors());         // Opens the API to frontend requests

// ==========================================
// 1. DATABASE CONNECTION
// ==========================================
// Mongoose will automatically create the 'store_orders' database
mongoose.connect('mongodb://localhost:27017/store_orders')
    .then(() => console.log('✅ Connected to MongoDB (store_orders)'))
    .catch(err => console.error('❌ MongoDB Connection Error:', err));

// ==========================================
// 2. MONGOOSE SCHEMAS & MODELS
// ==========================================

// The Sub-document: Represents a single item in the cart
const orderItemSchema = new mongoose.Schema({
    itemId: { type: String, required: true },
    name: { type: String, required: true },
    quantity: { type: Number, required: true, min: 1 },
    priceAtPurchase: { type: Number, required: true } 
}, { _id: false }); // We don't need Mongo to generate a unique ID for every line item

// The Main Document: Represents the entire order
const orderSchema = new mongoose.Schema({
    userId: { type: String, required: true }, // Maps to the PostgreSQL User ID
    totalAmount: { type: Number, required: true },
    items: [orderItemSchema],                 // The embedded array of items!
    status: { type: String, default: 'COMPLETED' },
    createdAt: { type: Date, default: Date.now }
});

const Order = mongoose.model('Order', orderSchema);

// ==========================================
// 3. API ROUTES
// ==========================================

// A simple test route to ensure the server is alive
app.get('/api/orders/health', (req, res) => {
    res.status(200).json({ message: 'Order Ingestion Service is running smoothly.' });
});

// Route to get all orders (Admin view) or orders for a specific user (Employee view)
app.get('/api/orders', async (req, res) => {
    try {
        // If the frontend passes a ?userId=... query, filter by it. Otherwise, return all.
        const filter = req.query.userId ? { userId: req.query.userId } : {};
        const orders = await Order.find(filter).sort({ createdAt: -1 }); // Newest first
        
        res.status(200).json(orders);
    } catch (error) {
        res.status(500).json({ error: 'Failed to fetch orders' });
    }
});

// POST: Create a new order AND communicate with Java
app.post('/api/orders', async (req, res) => {
    const { userId, items } = req.body;

    // 1. Basic Validation
    if (!userId || !items || items.length === 0) {
        return res.status(400).json({ error: 'Order must contain a userId and items.' });
    }

    try {
        // ==========================================
        // THE MICROSERVICE BRIDGE
        // ==========================================
        console.log(`[Order Service] Attempting to deduct inventory in Java for Order by ${userId}...`);
        
        let javaResponse;
        try {
            // Call the Java server (Port 8080)
            javaResponse = await fetch('http://localhost:8080/inventory-service-1.0-SNAPSHOT/api/items/decrement', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(items) // Send the array of items to Java
            });
        } catch (networkError) {
            // CRITICAL FAILURE: Java is offline!
            console.error("❌ Critical: Cannot reach Java Inventory Service", networkError.message);
            return res.status(503).json({ 
                error: 'Service Unavailable', 
                message: 'Cannot process order at this time: Inventory Service is offline.' 
            });
        }

        // Check if Java rejected the request (e.g., Insufficient Stock)
        // Check if Java rejected the request
        if (!javaResponse.ok) {
            let errorMessage = 'Java rejected the order, but did not return JSON.';
            
            // Check if Java actually sent JSON before trying to parse it
            const contentType = javaResponse.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                const errorData = await javaResponse.json();
                errorMessage = errorData.message || 'Not enough stock to complete this order.';
            } else {
                // If it's HTML (like a 404 page), just grab the text so we don't crash
                const htmlError = await javaResponse.text(); 
                console.error("❌ Tomcat Error Page Received:", htmlError.substring(0, 100) + "...");
            }

            console.error("❌ Java Rejected Order:", errorMessage);
            return res.status(400).json({ 
                error: 'Inventory Error', 
                message: errorMessage 
            });
        }

        // ==========================================
        // MONGODB PERSISTENCE
        // ==========================================
        console.log(`✅ Java approved inventory deduction. Saving order to MongoDB...`);

        // Calculate the total cost on the server to prevent frontend tampering
        const calculatedTotal = items.reduce((sum, item) => {
            return sum + (item.priceAtPurchase * item.quantity);
        }, 0);

        // Create the Mongoose Document
        const newOrder = new Order({
            userId: userId,
            totalAmount: calculatedTotal,
            items: items
        });

        // Save to MongoDB
        const savedOrder = await newOrder.save();
        
        // Respond to the Frontend
        res.status(201).json({
            message: "Order placed successfully!",
            order: savedOrder
        });

    } catch (error) {
        console.error("❌ Database Error:", error);
        res.status(500).json({ error: 'Internal Server Error while saving order to MongoDB.' });
    }
});

// ==========================================
// 4. START SERVER
// ==========================================

app.listen(PORT, () => {
    console.log(`🚀 Order Service is listening on http://localhost:${PORT}`);
});