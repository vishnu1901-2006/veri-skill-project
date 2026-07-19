import { Router, Request, Response } from 'express';
import pino from 'pino';
import type {
  ProveRequest,
  ProveResponse,
  VerifyRequest,
  VerifyResponse,
} from '../types/index.js';
import { ZKProofService } from '../services/ZKProofService.js';

const logger = pino();

export function createProofRoutes(zkService: ZKProofService): Router {
  const router = Router();

  /**
   * POST /prove
   * Generate a ZK proof that score >= threshold
   */
  router.post('/prove', async (req: Request, res: Response) => {
    try {
      logger.info({ body: req.body }, 'Request body received');
      const { candidate_id, private_score, required_threshold }: ProveRequest =
        req.body;

      logger.info(
        {
          candidateId: candidate_id,
          privateScore: private_score,
          requiredThreshold: required_threshold,
        },
        'Extracted fields from body'
      );

      if (!candidate_id || private_score === undefined || required_threshold === undefined) {
        return res.status(400).json({
          error: 'Missing required fields: candidate_id, private_score, required_threshold',
          received: req.body,
        });
      }

      logger.info(
        {
          candidateId: candidate_id,
          score: private_score,
          threshold: required_threshold,
        },
        'Received prove request'
      );

      const result = await zkService.generateProof({
        candidate_id,
        private_score,
        required_threshold,
      });

      if (!result.success || !result.proof) {
        return res.status(422).json({
          error: result.error || 'Failed to generate proof',
        });
      }

      const response: ProveResponse = {
        candidate_id,
        proof: result.proof,
        timestamp: Date.now(),
      };

      return res.status(200).json(response);
    } catch (error) {
      logger.error({ error }, 'Error in /prove endpoint');
      return res.status(500).json({
        error: error instanceof Error ? error.message : 'Internal server error',
      });
    }
  });

  /**
   * POST /verify
   * Verify a ZK proof
   */
  router.post('/verify', async (req: Request, res: Response) => {
    try {
      const { proof, private_score, required_threshold }: VerifyRequest =
        req.body;

      if (!proof || private_score === undefined || required_threshold === undefined) {
        return res.status(400).json({
          error: 'Missing required fields: proof, private_score, required_threshold',
        });
      }

      logger.info('Received verify request');

      const isValid = await zkService.verifyProof({
        proof,
        private_score,
        required_threshold,
      });

      const response: VerifyResponse = {
        valid: isValid,
        message: isValid
          ? 'Proof is valid: candidate meets requirements'
          : 'Proof is invalid',
      };

      return res.status(200).json(response);
    } catch (error) {
      logger.error({ error }, 'Error in /verify endpoint');
      return res.status(500).json({
        error: error instanceof Error ? error.message : 'Internal server error',
      });
    }
  });

  /**
   * GET /health
   * Health check endpoint
   */
  router.get('/health', (req: Request, res: Response) => {
    return res.status(200).json({
      status: 'ok',
      service: 'veri-skill-zk-bridge',
      timestamp: new Date().toISOString(),
    });
  });

  return router;
}
