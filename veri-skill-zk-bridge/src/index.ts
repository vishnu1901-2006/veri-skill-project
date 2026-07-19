import express, { Express } from 'express';
import pino from 'pino';
import { createProofRoutes } from './routes/proof.js';
import { ZKProofService } from './services/ZKProofService.js';

const logger = pino();

const app: Express = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Request logging middleware
app.use((req, res, next) => {
  logger.info(
    {
      method: req.method,
      url: req.url,
      ip: req.ip,
      headers: req.headers,
      contentType: req.get('Content-Type'),
      contentLength: req.get('Content-Length'),
    },
    'Incoming request'
  );
  next();
});

// Initialize ZK Proof Service
const zkService = new ZKProofService(
  process.env.MIDNIGHT_PROVE_URL || 'http://127.0.0.1:9944',
  process.env.MIDNIGHT_VERIFY_URL || 'http://127.0.0.1:9944'
);

// Routes
app.use('/', createProofRoutes(zkService));

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'veri-skill-zk-bridge',
    timestamp: new Date().toISOString(),
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    error: 'Not found',
    path: req.url,
  });
});

// Error handler
app.use(
  (
    err: Error,
    req: express.Request,
    res: express.Response,
    next: express.NextFunction
  ) => {
    logger.error({ error: err }, 'Unhandled error');
    res.status(500).json({
      error: 'Internal server error',
      message: err.message,
    });
  }
);

// Start server
const server = app.listen(port, () => {
  logger.info({ port }, 'Veri-Skill ZK Bridge service started');
});

// Graceful shutdown
process.on('SIGTERM', () => {
  logger.info('SIGTERM received, shutting down gracefully');
  server.close(() => {
    logger.info('Server closed');
    process.exit(0);
  });
});

process.on('SIGINT', () => {
  logger.info('SIGINT received, shutting down gracefully');
  server.close(() => {
    logger.info('Server closed');
    process.exit(0);
  });
});

export default app;
