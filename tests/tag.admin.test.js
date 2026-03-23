/**
 * 标签管理功能测试用例
 * 测试范围：标签 CRUD、状态切换、热门设置、分页查询
 */

const API_BASE_URL = 'http://localhost:8080';
let adminToken = '';

// 模拟管理员登录获取 Token
async function adminLogin() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: 'admin',
                password: 'admin123'
            })
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            adminToken = data.data || data.token;
            console.log('✓ 管理员登录成功');
            return true;
        } else {
            console.error('✗ 管理员登录失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 管理员登录异常:', error);
        return false;
    }
}

// 测试 1：查询标签列表
async function testGetTags() {
    console.log('\n=== 测试 1: 查询标签列表 ===');
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/tags?pageNum=1&pageSize=10&level=1`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 查询标签列表成功');
            console.log('  标签总数:', data.data?.total || data.result?.total);
            console.log('  当前页标签数:', (data.data?.records || data.result?.records || []).length);
            
            // 显示第一个标签的信息
            const firstTag = (data.data?.records || data.result?.records || [])[0];
            if (firstTag) {
                console.log('  示例标签:', firstTag.tagName, '(层级:', firstTag.level + ')');
            }
            return true;
        } else {
            console.error('✗ 查询标签列表失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 查询标签列表异常:', error);
        return false;
    }
}

// 测试 2：创建一级标签
async function testCreateLevel1Tag() {
    console.log('\n=== 测试 2: 创建一级标签 ===');
    
    const newTag = {
        level: 1,
        tagCode: 'TEST' + Date.now(),
        tagName: '测试标签-' + Date.now(),
        parentId: 0,
        icon: 'el-icon-star',
        color: '#FF6B6B',
        sortOrder: 100,
        isHot: 0,
        status: 1
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/tags`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(newTag)
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 创建一级标签成功');
            console.log('  标签 ID:', data.data?.id || data.result?.id);
            console.log('  标签名称:', data.data?.tagName || data.result?.tagName);
            console.log('  标签代码:', data.data?.tagCode || data.result?.tagCode);
            return data.data?.id || data.result?.id;
        } else {
            console.error('✗ 创建一级标签失败:', data.message);
            return null;
        }
    } catch (error) {
        console.error('✗ 创建一级标签异常:', error);
        return null;
    }
}

// 测试 3：创建二级标签
async function testCreateLevel2Tag(parentId) {
    console.log('\n=== 测试 3: 创建二级标签 (父级 ID=' + parentId + ') ===');
    
    const newTag = {
        level: 2,
        tagCode: 'TEST2-' + Date.now(),
        tagName: '测试二级标签-' + Date.now(),
        parentId: parentId,
        icon: 'el-icon-document',
        color: '#4A90E2',
        sortOrder: 1,
        isHot: 0,
        status: 1
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/tags`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(newTag)
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 创建二级标签成功');
            console.log('  标签 ID:', data.data?.id || data.result?.id);
            console.log('  标签名称:', data.data?.tagName || data.result?.tagName);
            console.log('  父级 ID:', data.data?.parentId || data.result?.parentId);
            return data.data?.id || data.result?.id;
        } else {
            console.error('✗ 创建二级标签失败:', data.message);
            return null;
        }
    } catch (error) {
        console.error('✗ 创建二级标签异常:', error);
        return null;
    }
}

// 测试 4：获取标签详情
async function testGetTag(id) {
    console.log('\n=== 测试 4: 获取标签详情 (ID=' + id + ') ===');
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/tags/${id}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 获取标签详情成功');
            console.log('  标签名称:', data.data?.tagName || data.result?.tagName);
            console.log('  标签层级:', data.data?.level || data.result?.level);
            console.log('  标签代码:', data.data?.tagCode || data.result?.tagCode);
            console.log('  颜色:', data.data?.color || data.result?.color);
            return true;
        } else {
            console.error('✗ 获取标签详情失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 获取标签详情异常:', error);
        return false;
    }
}

// 测试 5：更新标签
async function testUpdateTag(id) {
    console.log('\n=== 测试 5: 更新标签 (ID=' + id + ') ===');
    
    const updateData = {
        tagName: '更新后的测试标签',
        sortOrder: 99,
        color: '#50C878'
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/tags/${id}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updateData)
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 更新标签成功');
            console.log('  新标题:', data.data?.tagName || data.result?.tagName);
            console.log('  新排序:', data.data?.sortOrder || data.result?.sortOrder);
            console.log('  新颜色:', data.data?.color || data.result?.color);
            return true;
        } else {
            console.error('✗ 更新标签失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 更新标签异常:', error);
        return false;
    }
}

// 测试 6：设置热门标签
async function testSetHotTag(id) {
    console.log('\n=== 测试 6: 设置热门标签 (ID=' + id + ') ===');
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/tags/${id}/hot?isHot=1`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 设置热门标签成功');
            console.log('  热门状态：是');
            return true;
        } else {
            console.error('✗ 设置热门标签失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 设置热门标签异常:', error);
        return false;
    }
}

// 测试 7：更新标签状态
async function testUpdateTagStatus(id) {
    console.log('\n=== 测试 7: 更新标签状态 (ID=' + id + ') ===');
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/tags/${id}/status?status=0`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 更新标签状态成功');
            console.log('  新状态：禁用');
            return true;
        } else {
            console.error('✗ 更新标签状态失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 更新标签状态异常:', error);
        return false;
    }
}

// 测试 8：删除标签
async function testDeleteTag(id) {
    console.log('\n=== 测试 8: 删除标签 (ID=' + id + ') ===');
    
    try {
        const response = await fetch(`${API_BASE_URL}/admin/tags/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${adminToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        if (data.success || data.code === 200) {
            console.log('✓ 删除标签成功');
            return true;
        } else {
            console.error('✗ 删除标签失败:', data.message);
            return false;
        }
    } catch (error) {
        console.error('✗ 删除标签异常:', error);
        return false;
    }
}

// 主测试流程
async function runTagTests() {
    console.log('========================================');
    console.log('标签管理功能测试开始');
    console.log('========================================');
    
    // 1. 管理员登录
    const loginSuccess = await adminLogin();
    if (!loginSuccess) {
        console.error('\n✗ 管理员登录失败，无法继续测试');
        return;
    }
    
    // 2. 查询标签列表
    await testGetTags();
    
    // 3. 创建一级标签
    const level1Id = await testCreateLevel1Tag();
    if (!level1Id) {
        console.error('\n✗ 创建一级标签失败，无法继续后续测试');
        return;
    }
    
    // 4. 创建二级标签
    const level2Id = await testCreateLevel2Tag(level1Id);
    if (level2Id) {
        // 5. 获取标签详情（二级标签）
        await testGetTag(level2Id);
        
        // 6. 更新标签（二级标签）
        await testUpdateTag(level2Id);
        
        // 7. 设置热门标签（二级标签）
        await testSetHotTag(level2Id);
        
        // 8. 更新标签状态（二级标签）
        await testUpdateTagStatus(level2Id);
        
        // 9. 删除标签（先删除二级标签）
        await testDeleteTag(level2Id);
    }
    
    // 10. 删除一级标签
    await testDeleteTag(level1Id);
    
    console.log('\n========================================');
    console.log('标签管理功能测试完成');
    console.log('========================================');
}

// 运行测试
runTagTests().catch(console.error);
