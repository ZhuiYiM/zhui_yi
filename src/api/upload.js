import request from './request';

export const uploadAPI = {
    // 上传图片
    uploadImage(file) {
        const formData = new FormData();
        formData.append('file', file);
        return request.post('/upload/image', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    },

    // 上传多个图片
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

    // 删除已上传的文件
    deleteFile(fileUrl) {
        return request.delete('/upload/file', {
            data: { fileUrl }
        });
    }
};
