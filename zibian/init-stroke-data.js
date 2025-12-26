#!/usr/bin/env node

import fs from 'fs';
import path from 'path';
import fetch from 'node-fetch';

// Configuration
const WORD_BOOKS = [
    './docs/ciben/启蒙词本300.txt',
    './docs/ciben/小学词本1000.txt',
    './docs/ciben/中学词本3500.txt'
];
const OUTPUT_DIR = './public/data/hanzi';
const HANZI_WRITER_CDN = 'https://cdn.jsdelivr.net/npm/hanzi-writer-data@2.0';
const BATCH_SIZE = 5; // Reduce batch size to avoid overwhelming CDN
const REQUEST_TIMEOUT = 5000; // 5 seconds timeout for each request
const DELAY_BETWEEN_BATCHES = 1000; // 1 second delay between batches
const DELAY_BETWEEN_REQUESTS = 200; // 200ms delay between individual requests

// Ensure output directory exists
if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
    console.log(`Created output directory: ${OUTPUT_DIR}`);
}

// Read word book files
function readWordBooks() {
    try {
        let allCharacters = [];
        const scriptPath = import.meta.url.replace('file:', '');
        const scriptDir = path.dirname(scriptPath);
        
        // Read all word books
        for (const wordBookPath of WORD_BOOKS) {
            // Build absolute path by going up one level from script directory
            const fullPath = path.join(scriptDir, '..', wordBookPath);
            console.log(`Reading word book: ${fullPath}`);
            const content = fs.readFileSync(fullPath, 'utf8');
            // Extract all Chinese characters from the content
            const characters = content.match(/[\u4e00-\u9fa5]/g) || [];
            allCharacters = [...allCharacters, ...characters];
            console.log(`Found ${characters.length} characters in ${wordBookPath}`);
        }
        
        // Remove duplicates
        const uniqueCharacters = [...new Set(allCharacters)];
        
        console.log(`\nTotal unique Chinese characters across all word books: ${uniqueCharacters.length}`);
        return uniqueCharacters;
    } catch (error) {
        console.error(`Error reading word books: ${error.message}`);
        process.exit(1);
    }
}

// Helper function with timeout
async function fetchWithTimeout(url, options = {}, timeout = REQUEST_TIMEOUT) {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeout);
    
    try {
        const response = await fetch(url, { ...options, signal: controller.signal });
        clearTimeout(timeoutId);
        return response;
    } catch (error) {
        clearTimeout(timeoutId);
        throw error;
    }
}

// Download stroke data for a character
async function downloadStrokeData(character) {
    const url = `${HANZI_WRITER_CDN}/${character}.json`;
    const outputPath = path.join(OUTPUT_DIR, `${character}.json`);
    
    try {
        // Check if file already exists
        if (fs.existsSync(outputPath)) {
            console.log(`✓ ${character}.json already exists, skipping`);
            return;
        }
        
        // Add delay between requests
        await new Promise(resolve => setTimeout(resolve, DELAY_BETWEEN_REQUESTS));
        
        const response = await fetchWithTimeout(url);
        
        if (response.ok) {
            const data = await response.json();
            fs.writeFileSync(outputPath, JSON.stringify(data, null, 2));
            console.log(`✓ Downloaded ${character}.json`);
        } else {
            console.log(`✗ Failed to download ${character}.json: ${response.status}`);
        }
    } catch (error) {
        if (error.name === 'AbortError') {
            console.log(`✗ Timeout downloading ${character}.json (${REQUEST_TIMEOUT}ms)`);
        } else {
            console.log(`✗ Error downloading ${character}.json: ${error.message}`);
        }
    }
}

// Main function
async function main() {
    console.log('=== Hanzi Stroke Data Initialization ===\n');
    
    const characters = readWordBooks();
    const totalCharacters = characters.length;
    
    console.log('\nDownloading stroke data from HanziWriter CDN...');
    console.log('=' . repeat(60));
    
    let downloadedCount = 0;
    let existingCount = 0;
    let failedCount = 0;
    
    // Download characters in batches to avoid rate limiting
    for (let i = 0; i < totalCharacters; i += BATCH_SIZE) {
        const batch = characters.slice(i, i + BATCH_SIZE);
        const batchStart = i + 1;
        const batchEnd = Math.min(i + BATCH_SIZE, totalCharacters);
        
        console.log(`\nProcessing batch ${Math.floor(i/BATCH_SIZE) + 1}/${Math.ceil(totalCharacters/BATCH_SIZE)} (${batchStart}-${batchEnd}/${totalCharacters})`);
        
        await Promise.all(batch.map(downloadStrokeData));
        
        // Update progress counts
        const currentFiles = fs.readdirSync(OUTPUT_DIR).filter(file => file.endsWith('.json'));
        downloadedCount = currentFiles.length;
        
        // Wait between batches
        if (i + BATCH_SIZE < totalCharacters) {
            console.log(`Waiting ${DELAY_BETWEEN_BATCHES}ms before next batch...`);
            await new Promise(resolve => setTimeout(resolve, DELAY_BETWEEN_BATCHES));
        }
    }
    
    console.log('\n' + '=' . repeat(60));
    console.log('\n=== Initialization Complete ===');
    
    // Count downloaded files
    const downloadedFiles = fs.readdirSync(OUTPUT_DIR).filter(file => file.endsWith('.json'));
    console.log(`\nSummary:`);
    console.log(`- Total characters to process: ${totalCharacters}`);
    console.log(`- Successfully downloaded: ${downloadedFiles.length}`);
    console.log(`- Files are saved in: ${OUTPUT_DIR}`);
    console.log(`- Batch size: ${BATCH_SIZE}`);
    console.log(`- Delay between batches: ${DELAY_BETWEEN_BATCHES}ms`);
    console.log(`- Delay between requests: ${DELAY_BETWEEN_REQUESTS}ms`);
}

// Run main function
main().catch(error => {
    console.error(`Unexpected error: ${error.message}`);
    process.exit(1);
});
