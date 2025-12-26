package com.gewujie.zibian.util;

import com.gewujie.zibian.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 手动更新字符数据工具类
 * 用于修复lessons表中的模拟数据
 */
@Component
public class UpdateCharacterDataTool implements CommandLineRunner {

    @Autowired
    private LearningService learningService;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否有命令行参数触发更新
        if (args != null && args.length > 0 && "--update-character-data".equals(args[0])) {
            System.out.println("开始手动更新字符数据...");
            learningService.updateAllLessonsCharacterData();
            System.out.println("字符数据更新完成！");
        }
    }
}
