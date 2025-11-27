import os
import re

def update_imports(file_path):
    """Update import paths in a Java file"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    original_content = content

    # Pattern replacements
    replacements = [
        (r'com\.ts\.rm\.global\.common\.exception', 'com.ts.rm.global.exception'),
        (r'com\.ts\.rm\.global\.common\.filter', 'com.ts.rm.global.filter'),
        (r'com\.ts\.rm\.global\.common\.response', 'com.ts.rm.global.response'),
        (r'com\.ts\.rm\.global\.common\.util', 'com.ts.rm.global.util'),
        (r'com\.ts\.rm\.global\.repository\.CodeRepository', 'com.ts.rm.domain.common.repository.CodeRepository'),
    ]

    for pattern, replacement in replacements:
        content = re.sub(pattern, replacement, content)

    # Only write if content changed
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        return True
    return False

def main():
    """Update all Java files in src directory"""
    src_dir = r'C:\Soronprfbs\project\release-manager\src'
    updated_count = 0

    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                if update_imports(file_path):
                    updated_count += 1
                    print(f'Updated: {file_path}')

    print(f'\nTotal files updated: {updated_count}')

if __name__ == '__main__':
    main()
