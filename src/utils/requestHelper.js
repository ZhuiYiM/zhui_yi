// 请求工具函数

/**
 * 处理分页参数
 * @param {Object} params - 原始参数
 * @param {number} page - 页码
 * @param {number} size - 每页大小
 * @returns {Object} 处理后的参数
 */
export const handlePagination = (params = {}, page = 1, size = 10) => {
    return {
        ...params,
        page,
        size
    };
};

/**
 * 处理搜索参数
 * @param {Object} params - 原始参数
 * @param {string} keyword - 搜索关键词
 * @returns {Object} 处理后的参数
 */
export const handleSearch = (params = {}, keyword = '') => {
    if (keyword) {
        return {
            ...params,
            keyword
        };
    }
    return params;
};

/**
 * 处理排序参数
 * @param {Object} params - 原始参数
 * @param {string} sortBy - 排序字段
 * @param {string} sortOrder - 排序顺序 (asc/desc)
 * @returns {Object} 处理后的参数
 */
export const handleSort = (params = {}, sortBy = '', sortOrder = 'desc') => {
    if (sortBy) {
        return {
            ...params,
            sortBy,
            sortOrder
        };
    }
    return params;
};

/**
 * 处理筛选参数
 * @param {Object} params - 原始参数
 * @param {Object} filters - 筛选条件
 * @returns {Object} 处理后的参数
 */
export const handleFilter = (params = {}, filters = {}) => {
    return {
        ...params,
        ...filters
    };
};

/**
 * 构建完整的请求参数
 * @param {Object} options - 配置选项
 * @returns {Object} 完整的请求参数
 */
export const buildRequestParams = (options = {}) => {
    const {
        page = 1,
        size = 10,
        keyword = '',
        sortBy = '',
        sortOrder = 'desc',
        filters = {},
        extraParams = {}
    } = options;

    let params = {};

    // 处理分页
    params = handlePagination(params, page, size);

    // 处理搜索
    params = handleSearch(params, keyword);

    // 处理排序
    params = handleSort(params, sortBy, sortOrder);

    // 处理筛选
    params = handleFilter(params, filters);

    // 添加额外参数
    params = { ...params, ...extraParams };

    return params;
};

/**
 * 格式化响应数据
 * @param {Object} response - 原始响应
 * @returns {Object} 格式化后的数据
 */
export const formatResponse = (response) => {
    if (response && response.code === 200) {
        return {
            success: true,
            data: response.data,
            message: response.message || '操作成功'
        };
    } else {
        return {
            success: false,
            data: null,
            message: response?.message || '操作失败'
        };
    }
};

/**
 * 处理文件上传
 * @param {Array|File} files - 文件列表或单个文件
 * @returns {FormData} 表单数据
 */
export const prepareUploadData = (files) => {
    const formData = new FormData();
    
    if (Array.isArray(files)) {
        files.forEach(file => {
            formData.append('files', file);
        });
    } else {
        formData.append('file', files);
    }
    
    return formData;
};