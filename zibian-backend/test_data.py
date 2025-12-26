import psycopg2
import sys

# Database connection parameters
conn_params = {
    'dbname': 'zibian',
    'user': 'postgres',
    'password': 'Aa123456',
    'host': '10.132.152.2',
    'port': '6111'
}

try:
    # Connect to the database
    conn = psycopg2.connect(**conn_params)
    cur = conn.cursor()
    
    print("Connected to database successfully!")
    print("\n=== Testing data fields ===")
    
    # Test 1: Check if there are lessons with non-empty fields
    cur.execute("""
        SELECT character, pinyin, definition, words 
        FROM lessons 
        WHERE pinyin != '' AND definition != '' AND words != '' 
        LIMIT 10
    """)
    
    non_empty_results = cur.fetchall()
    if non_empty_results:
        print("\n✓ Found lessons with populated fields:")
        for row in non_empty_results:
            char, pinyin, definition, words = row
            print(f"  字: {char}, 拼音: {pinyin}, 释义: {definition}, 词语: {words}")
    else:
        print("\n✗ No lessons found with populated fields!")
    
    # Test 2: Check total lessons count
    cur.execute("SELECT COUNT(*) FROM lessons")
    total_count = cur.fetchone()[0]
    print(f"\n✓ Total lessons in database: {total_count}")
    
    # Test 3: Check lessons by textbook category
    cur.execute("SELECT textbook_category, COUNT(*) FROM lessons GROUP BY textbook_category")
    category_counts = cur.fetchall()
    print("\n✓ Lessons by category:")
    for category, count in category_counts:
        print(f"  {category}: {count} lessons")
    
    # Test 4: Check if any lessons still have empty fields
    cur.execute("""
        SELECT COUNT(*) 
        FROM lessons 
        WHERE pinyin = '' OR definition = '' OR words = ''
    """)
    empty_fields_count = cur.fetchone()[0]
    
    if empty_fields_count > 0:
        print(f"\n⚠️  There are {empty_fields_count} lessons with empty fields (expected for characters not in mapping)")
        
        # Show some examples of lessons with empty fields
        cur.execute("""
            SELECT character, pinyin, definition, words 
            FROM lessons 
            WHERE pinyin = '' OR definition = '' OR words = '' 
            LIMIT 5
        """)
        empty_examples = cur.fetchall()
        print("\nExamples of lessons with empty fields:")
        for row in empty_examples:
            char, pinyin, definition, words = row
            print(f"  字: {char}, 拼音: '{pinyin}', 释义: '{definition}', 词语: '{words}'")
    else:
        print("\n✓ All lessons have populated fields!")
    
    # Close the connection
    cur.close()
    conn.close()
    
    print("\n=== Test completed successfully! ===")
    sys.exit(0)
    
except psycopg2.Error as e:
    print(f"Error connecting to database: {e}")
    sys.exit(1)
