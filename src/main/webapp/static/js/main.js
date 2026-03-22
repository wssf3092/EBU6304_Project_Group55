/**
 * main.js - 全局通用交互与校验逻辑
 */

// DOM 加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    highlightCurrentNav();
});

/**
 * 根据当前 URL 自动高亮对应的导航栏项
 */
function highlightCurrentNav() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-link');
    
    // Default Home active if at root or unspecified
    let matched = false;
    navLinks.forEach(link => {
        const pageKey = link.getAttribute('data-page');
        if (pageKey && currentPath.includes(pageKey)) {
            link.classList.add('active');
            matched = true;
        } else {
            link.classList.remove('active');
        }
    });

    if(!matched && navLinks.length > 0) {
        // Option to default to dashboard
        const dashboardLink = document.querySelector('.nav-link[data-page="dashboard"]');
        if (dashboardLink) dashboardLink.classList.add('active');
    }
}

/**
 * 通用确认弹窗
 * 使用场景：删除、注销、重要提交前
 * @param {string} message 提示信息
 * @returns {boolean} 用户是否点击了确认
 */
function confirmAction(message) {
    return window.confirm(message || "确定执行此操作吗？");
}

/**
 * 校验并经过确认后提交表单
 * @param {string} message 提示信息
 * @returns {boolean} 是否允许提交
 */
function validateAndConfirmSubmit(message) {
    return confirmAction(message);
}
