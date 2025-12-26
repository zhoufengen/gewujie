import psycopg2
import psycopg2.extras

# 数据库连接参数
conn_params = {
    'host': '10.132.152.2',  # 从application.yml获取的数据库地址
    'port': '6111',         # 从application.yml获取的数据库端口
    'database': 'zibian',   # 从application.yml获取的数据库名
    'user': 'postgres',     # 从application.yml获取的用户名
    'password': 'Aa123456'  # 从application.yml获取的密码
}

try:
    # 连接数据库
    conn = psycopg2.connect(**conn_params)
    cursor = conn.cursor(cursor_factory=psycopg2.extras.DictCursor)
    
    print("成功连接到数据库")
    
    # 检查表是否存在
    cursor.execute("SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'lessons')")
    table_exists = cursor.fetchone()[0]
    
    if not table_exists:
        print("表 'lessons' 不存在")
    else:
        print("表 'lessons' 存在")
        
        # 查询表结构
        cursor.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'lessons' ORDER BY ordinal_position")
        columns = cursor.fetchall()
        print("\n表结构:")
        for col in columns:
            print(f"- {col['column_name']} ({col['data_type']})")
        
        # 查询前10条记录，检查字段数据
        cursor.execute("SELECT id, character, pinyin, definition, words, textbook_category FROM lessons LIMIT 10")
        records = cursor.fetchall()
        
        print(f"\n前10条记录:")
        for record in records:
            print(f"ID: {record['id']}")
            print(f"字符: {record['character']}")
            print(f"拼音: '{record['pinyin']}'")
            print(f"释义: '{record['definition']}'")
            print(f"词语: '{record['words']}'")
            print(f"词本分类: {record['textbook_category']}")
            print("-" * 30)
        
        # 统计记录数量
        cursor.execute("SELECT COUNT(*) FROM lessons")
        total_records = cursor.fetchone()[0]
        print(f"\n总记录数: {total_records}")
        
        # 统计有拼音数据的记录数量
        cursor.execute("SELECT COUNT(*) FROM lessons WHERE pinyin IS NOT NULL AND pinyin != ''")
        pinyin_count = cursor.fetchone()[0]
        print(f"有拼音数据的记录数: {pinyin_count}")
        
        # 统计有释义数据的记录数量
        cursor.execute("SELECT COUNT(*) FROM lessons WHERE definition IS NOT NULL AND definition != ''")
        definition_count = cursor.fetchone()[0]
        print(f"有释义数据的记录数: {definition_count}")
        
        # 统计有词语数据的记录数量
        cursor.execute("SELECT COUNT(*) FROM lessons WHERE words IS NOT NULL AND words != ''")
        words_count = cursor.fetchone()[0]
        print(f"有词语数据的记录数: {words_count}")
        
        # 检查是否有记录的三个字段都为空
        cursor.execute("SELECT COUNT(*) FROM lessons WHERE (pinyin IS NULL OR pinyin = '') AND (definition IS NULL OR definition = '') AND (words IS NULL OR words = '')")
        all_empty_count = cursor.fetchone()[0]
        print(f"三个字段都为空的记录数: {all_empty_count}")
        
        # 检查词本分类分布
        cursor.execute("SELECT textbook_category, COUNT(*) FROM lessons GROUP BY textbook_category")
        categories = cursor.fetchall()
        print(f"\n词本分类分布:")
        for cat in categories:
            print(f"- {cat['textbook_category']}: {cat['count']}")

except Exception as e:
    print(f"数据库操作错误: {e}")
finally:
    if 'cursor' in locals():
        cursor.close()
    if 'conn' in locals():
        conn.close()
    print("\n数据库连接已关闭")