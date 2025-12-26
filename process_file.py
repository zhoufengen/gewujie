with open('docs/ciben/中学词本1000.txt', 'r', encoding='utf-8') as f:
    content = f.read().strip()

# 将每个字符用、号隔开
new_content = '、'.join(list(content))

with open('docs/ciben/中学词本1000.txt', 'w', encoding='utf-8') as f:
    f.write(new_content)

print('文件处理完成！')