// Simple test runner for Bun
import { test, expect } from 'bun:test';
import { runTests } from './bun-adapter';

test('Run Scala.js tests', async () => {
    try {
        console.log('Starting Scala.js tests...');
        const result = await runTests();
        expect(result).toBe(true);
    } catch (error) {
        console.error('Test execution failed:', error);
        throw error;
    }
}); 