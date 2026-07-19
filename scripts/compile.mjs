import { execSync } from 'node:child_process';
import { platform } from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const projectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const compactSource = 'contracts/VeriSkill.compact';
const compactOutput = 'contracts/managed/VeriSkill';
const compactArgs = `compile ${compactSource} ${compactOutput}`;

function toWslPath(windowsPath) {
  const normalized = path.resolve(windowsPath).replace(/\\/g, '/');
  const drive = normalized[0]?.toLowerCase();
  if (!drive || normalized[1] !== ':') {
    return normalized;
  }
  return `/mnt/${drive}${normalized.slice(2)}`;
}

if (platform() === 'win32') {
  const wslProjectRoot = toWslPath(projectRoot);
  execSync(
    `wsl bash -lc "cd '${wslProjectRoot}' && compact ${compactArgs}"`,
    { stdio: 'inherit' },
  );
} else {
  execSync(`compact ${compactArgs}`, { cwd: projectRoot, stdio: 'inherit' });
}
