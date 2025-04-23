// Custom test adapter for Node.js to handle Scala.js test infrastructure
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Initialize global Scala.js environment
global.scala = {
    Predef: {
        println: (msg) => console.log(msg)
    }
};

// Initialize test configuration
global.scalajsTestConfig = {
    moduleKind: "ESModule",
    moduleSplitStyle: "SmallModulesFor",
    outputPatterns: {
        jsFile: "[module].js",
        sourceMapFile: "[module].js.map"
    }
};

// Initialize Scala.js test environment
global.scalajsCom = {
    init: (callback) => {
        console.log('Initializing Scala.js test environment...');
        try {
            console.log('scalajsCom.init: Starting callback');
            callback();
            console.log('scalajsCom.init: Callback completed');
        } catch (error) {
            console.error('Error in scalajsCom.init:', error);
            throw error;
        }
    }
};

// Initialize the test bridge
global.scalajsTestBridge = {
    init: (callback) => {
        console.log('Initializing Scala.js test bridge...');
        try {
            console.log('scalajsTestBridge.init: Starting callback');
            callback();
            console.log('scalajsTestBridge.init: Callback completed');
        } catch (error) {
            console.error('Error in scalajsTestBridge.init:', error);
            throw error;
        }
    },
    error: (msg) => {
        console.error('Test bridge error:', msg);
        throw new Error(msg);
    },
    log: (msg) => {
        console.log('Test bridge log:', msg);
    },
    send: (msg) => {
        console.log('Test bridge send:', msg);
    }
};

// Initialize the test adapter
global.scalajsTestAdapter = {
    init: (callback) => {
        console.log('Initializing Scala.js test adapter...');
        try {
            console.log('scalajsTestAdapter.init: Starting callback');
            callback();
            console.log('scalajsTestAdapter.init: Callback completed');
        } catch (error) {
            console.error('Error in scalajsTestAdapter.init:', error);
            throw error;
        }
    },
    error: (msg) => {
        console.error('Test adapter error:', msg);
        throw new Error(msg);
    },
    log: (msg) => {
        console.log('Test adapter log:', msg);
    }
};

// Initialize the test runner
global.scalajsTestRunner = {
    init: (callback) => {
        console.log('Initializing Scala.js test runner...');
        try {
            console.log('scalajsTestRunner.init: Starting callback');
            callback();
            console.log('scalajsTestRunner.init: Callback completed');
        } catch (error) {
            console.error('Error in scalajsTestRunner.init:', error);
            throw error;
        }
    },
    error: (msg) => {
        console.error('Test runner error:', msg);
        throw new Error(msg);
    },
    log: (msg) => {
        console.log('Test runner log:', msg);
    }
};

async function runTests() {
    try {
        // Use absolute path to the test bundle
        const bundlePath = join(__dirname, '../../../dist/main.js');
        console.log('Loading test bundle from:', bundlePath);

        // Initialize the test environment
        await new Promise((resolve, reject) => {
            try {
                console.log('Starting test environment initialization...');
                global.scalajsCom.init(() => {
                    console.log('scalajsCom initialized, proceeding to test bridge...');
                    global.scalajsTestBridge.init(() => {
                        console.log('scalajsTestBridge initialized, proceeding to test adapter...');
                        global.scalajsTestAdapter.init(() => {
                            console.log('scalajsTestAdapter initialized, proceeding to test runner...');
                            global.scalajsTestRunner.init(resolve);
                        });
                    });
                });
            } catch (error) {
                console.error('Error during initialization:', error);
                reject(error);
            }
        });

        // Load and run the test bundle
        console.log('Test environment initialized, loading test bundle...');
        await import(bundlePath);
        console.log('Test bundle loaded successfully');
        return true;
    } catch (error) {
        console.error("Error running tests:", error);
        throw error;
    }
}

// Run tests if this file is executed directly
if (import.meta.url === `file://${process.argv[1]}`) {
    runTests().catch(error => {
        console.error('Test execution failed:', error);
        process.exit(1);
    });
}

export { runTests }; 