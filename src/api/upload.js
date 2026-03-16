import request from './request';

export const uploadAPI = {
    /**
     * 上传单张图片
     * @param {File} file - 图片文件
     * @returns {Promise} 返回上传结果
     */
    uploadImage(file) {
        const formData = new FormData();
        formData.append('file', file);
        
        // 删除默认的 Content-Type，让 axios 自动设置为 multipart/form-data 并添加 boundary
        return request.post('/upload/image', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    },

    /**
     * 批量上传图片
     * @param {File[]} files - 图片文件数组
     * @returns {Promise} 返回上传结果
     */
    uploadMultipleImages(files) {
        const formData = new FormData();
        files.forEach(file => {
            formData.append('files', file);
        });
        return request.post('/upload/images', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    },

    /**
     * 删除已上传的文件
     * @param {string} fileUrl - 文件 URL
     * @returns {Promise} 返回删除结果
     */
    deleteFile(fileUrl) {
        return request.delete('/upload/file', {
            data: { fileUrl }
        });
    },

    /**
     * 压缩图片（前端处理）
     * @param {File} file - 原始图片文件
     * @param {number} maxWidth - 最大宽度
     * @param {number} maxHeight - 最大高度
     * @param {number} quality - 压缩质量 (0-1)
     * @returns {Promise<File>} 返回压缩后的文件
     */
    compressImage(file, maxWidth = 1920, maxHeight = 1080, quality = 0.8) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            
            reader.onload = (e) => {
                const img = new Image();
                img.src = e.target.result;
                
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    let width = img.width;
                    let height = img.height;

                    // 计算缩放比例
                    if (width > maxWidth || height > maxHeight) {
                        const ratio = Math.min(maxWidth / width, maxHeight / height);
                        width = Math.floor(width * ratio);
                        height = Math.floor(height * ratio);
                    }

                    canvas.width = width;
                    canvas.height = height;
                    
                    const ctx = canvas.getContext('2d');
                    ctx.drawImage(img, 0, 0, width, height);
                    
                    canvas.toBlob(
                        (blob) => {
                            if (blob) {
                                const compressedFile = new File([blob], file.name, {
                                    type: 'image/jpeg',
                                    lastModified: Date.now()
                                });
                                resolve(compressedFile);
                            } else {
                                reject(new Error('图片压缩失败'));
                            }
                        },
                        'image/jpeg',
                        quality
                    );
                };
                
                img.onerror = () => {
                    reject(new Error('图片加载失败'));
                };
            };
            
            reader.onerror = () => {
                reject(new Error('文件读取失败'));
            };
        });
    }
};
