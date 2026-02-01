// 主JavaScript文件
document.addEventListener('DOMContentLoaded', function () {
    console.log('SpringBoot2 路由示例应用已加载');

    // 为所有链接添加点击事件，显示请求信息
    const links = document.querySelectorAll('a');
    links.forEach(link => {
        // 排除外部链接和静态资源链接
        if (link.getAttribute('href').startsWith('/api/') ||
            link.getAttribute('href').startsWith('/basic/') ||
            link.getAttribute('href').startsWith('/advanced/')) {

            link.addEventListener('click', function (e) {
                // 对于某些需要在页面内显示结果的链接，阻止默认行为
                if (!link.classList.contains('external')) {
                    e.preventDefault();

                    const url = link.getAttribute('href');
                    console.log('请求URL:', url);

                    // 发送AJAX请求获取数据
                    fetch(url)
                        .then(response => {
                            if (response.headers.get('content-type').includes('application/json')) {
                                return response.json();
                            } else {
                                return response.text();
                            }
                        })
                        .then(data => {
                            // 在链接下方显示响应
                            let responseDiv = link.parentNode.querySelector('.response');
                            if (!responseDiv) {
                                responseDiv = document.createElement('div');
                                responseDiv.className = 'response';
                                link.parentNode.appendChild(responseDiv);
                            }

                            if (typeof data === 'object') {
                                responseDiv.textContent = JSON.stringify(data, null, 2);
                            } else {
                                responseDiv.textContent = data;
                            }
                        })
                        .catch(error => {
                            console.error('请求错误:', error);
                        });
                }
            });
        }
    });
});