// 专门用于替换棕色值(127,78,22)的JavaScript脚本

/**
 * 替换所有包含特定棕色值的内联样式
 */
function replaceBrownWithGray() {
    console.log('开始替换棕色值(127,78,22)为灰色...');
    
    // 定义要查找的棕色值和替换的灰色值
    const targetBrown = '127,78,22';
    const targetGray = '#454545';
    
    // 查找所有元素
    const allElements = document.querySelectorAll('*');
    let replacedCount = 0;
    
    // 正则表达式匹配不同格式的颜色值
    const colorRegexes = [
        // 匹配rgba格式带透明度
        new RegExp(`rgba\(\s*${targetBrown}\s*,\s*[^)]*\)`, 'gi'),
        // 匹配rgb格式不带透明度
        new RegExp(`rgb\(\s*${targetBrown}\s*\)`, 'gi'),
        // 匹配十六进制格式（先转换为十六进制）
        new RegExp('#7f4e16', 'gi')
    ];
    
    // 遍历所有元素
    allElements.forEach(element => {
        // 处理元素的style属性
        if (element.hasAttribute('style')) {
            let style = element.getAttribute('style');
            const originalStyle = style;
            
            // 检查是否包含目标棕色值
            if (style.includes(targetBrown)) {
                // 替换背景色
                style = style.replace(/background-color:\s*rgba\(127,\s*78,\s*22[^)]*\)/gi, `background-color: ${targetGray}`);
                style = style.replace(/background:\s*rgba\(127,\s*78,\s*22[^)]*\)/gi, `background: ${targetGray}`);
                
                // 替换边框色
                style = style.replace(/border-color:\s*rgba\(127,\s*78,\s*22[^)]*\)/gi, `border-color: ${targetGray}`);
                style = style.replace(/border:\s*[^;]*rgba\(127,\s*78,\s*22[^)]*\)/gi, match => {
                    return match.replace(/rgba\(127,\s*78,\s*22[^)]*\)/gi, targetGray);
                });
                
                // 替换文字颜色
                style = style.replace(/color:\s*rgba\(127,\s*78,\s*22[^)]*\)/gi, `color: ${targetGray}`);
                
                // 替换其他可能的颜色属性
                style = style.replace(/fill:\s*rgba\(127,\s*78,\s*22[^)]*\)/gi, `fill: ${targetGray}`);
                style = style.replace(/stroke:\s*rgba\(127,\s*78,\s*22[^)]*\)/gi, `stroke: ${targetGray}`);
                
                // 如果样式发生了变化，更新元素的style属性
                if (style !== originalStyle) {
                    element.setAttribute('style', style);
                    replacedCount++;
                }
            }
        }
        
        // 处理元素的class属性（查找可能包含棕色的类名）
        if (element.className && typeof element.className === 'string') {
            if (element.className.includes('brown') || 
                element.className.includes('chocolate') || 
                element.className.includes('coffee')) {
                // 为这些元素添加一个强制灰色的类
                element.classList.add('force-gray');
            }
        }
        
        // 检查data属性，有些样式可能存储在data属性中
        Array.from(element.attributes).forEach(attr => {
            if (attr.name.startsWith('data-') && 
                attr.value && 
                attr.value.includes(targetBrown)) {
                // 替换data属性中的颜色值
                const newVal = attr.value.replace(new RegExp(targetBrown, 'gi'), '69,69,69');
                element.setAttribute(attr.name, newVal);
                replacedCount++;
            }
        });
    });
    
    console.log(`已成功替换${replacedCount}个包含棕色值(127,78,22)的元素`);
}

/**
 * 添加一个强制灰色的样式规则到页面
 */
function addForceGrayStyle() {
    // 检查是否已经存在这个样式
    if (!document.getElementById('force-gray-style')) {
        const style = document.createElement('style');
        style.id = 'force-gray-style';
        style.textContent = `
            /* 强制灰色样式规则 */
            .force-gray {
                background-color: #454545 !important;
                border-color: #454545 !important;
                color: inherit !important;
                fill: #454545 !important;
                stroke: #454545 !important;
            }
            
            /* 针对特定棕色值的全局覆盖 */
            *[style*="127,78,22"] {
                filter: hue-rotate(60deg) grayscale(40%) !important;
            }
        `;
        document.head.appendChild(style);
    }
}

/**
 * 监听DOM变化，确保动态加载的内容也能被替换
 */
function setupMutationObserver() {
    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.addedNodes.length > 0) {
                // 有新节点添加时，延迟执行替换以确保节点完全加载
                setTimeout(replaceBrownWithGray, 100);
            }
        });
    });
    
    // 观察整个文档的变化
    observer.observe(document.body, {
        childList: true,
        subtree: true
    });
}

/**
 * 初始化颜色替换功能
 */
function initBrownToGrayReplacer() {
    // 添加强制灰色样式
    addForceGrayStyle();
    
    // 立即执行一次替换
    replaceBrownWithGray();
    
    // 设置定时器，每隔一段时间再次执行替换，确保动态生成的内容也能被替换
    setInterval(replaceBrownWithGray, 2000);
    
    // 设置DOM变化监听
    setupMutationObserver();
    
    console.log('棕色到灰色替换器已初始化');
}

// 在页面加载完成后初始化
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initBrownToGrayReplacer);
} else {
    // 页面已经加载完成，直接初始化
    initBrownToGrayReplacer();
}