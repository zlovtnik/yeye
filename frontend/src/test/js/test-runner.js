// Custom test runner for Bun
import { test, expect } from 'bun:test';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Import the Scala.js test bundle
const testBundle = join(__dirname, '../../target/scala-3.3.1/yeye-frontend-test-fastopt/main.js');

test('Run Scala.js tests', async () => {
    const { default: testModule } = await import(testBundle);
    expect(testModule).toBeDefined();
}); 