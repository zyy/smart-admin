package net.lab1024.smartadmin.service.module.support.file.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import net.lab1024.smartadmin.service.common.codeconst.FileResponseCodeConst;
import net.lab1024.smartadmin.service.common.codeconst.ResponseCodeConst;
import net.lab1024.smartadmin.service.common.constant.CommonConst;
import net.lab1024.smartadmin.service.common.constant.NumberLimitConst;
import net.lab1024.smartadmin.service.common.constant.RedisKeyConst;
import net.lab1024.smartadmin.service.common.domain.PageResultDTO;
import net.lab1024.smartadmin.service.common.domain.ResponseDTO;
import net.lab1024.smartadmin.service.module.support.file.FileDao;
import net.lab1024.smartadmin.service.module.support.file.domain.FileEntity;
import net.lab1024.smartadmin.service.module.support.file.domain.FileFolderTypeEnum;
import net.lab1024.smartadmin.service.module.support.file.domain.dto.*;
import net.lab1024.smartadmin.service.module.support.file.domain.vo.FileUploadVO;
import net.lab1024.smartadmin.service.module.support.file.domain.vo.FileVO;
import net.lab1024.smartadmin.service.third.SmartRedisService;
import net.lab1024.smartadmin.service.util.SmartBaseEnumUtil;
import net.lab1024.smartadmin.service.util.SmartBeanUtil;
import net.lab1024.smartadmin.service.util.SmartPageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [  ]
 *
 * @author 罗伊
 * @date 2020/8/25 11:57
 */
@Service
public class FileService {

    @Resource
    private IFileStorageService fileStorageService;

    @Autowired
    private FileDao fileDao;

    @Autowired
    private SmartRedisService redisService;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    /**
     * 文件上传服务：通过 url 上传
     *
     * @param urlUploadDTO
     * @return
     */
    public ResponseDTO<FileUploadVO> fileUpload(FileUrlUploadDTO urlUploadDTO) {
        try {
            URL url = new URL(urlUploadDTO.getFileUrl());
            URLConnection urlConnection = url.openConnection();
            // 获取文件格式
            String contentType = urlConnection.getContentType();
            String fileType = fileStorageService.getFileTypeByContentType(contentType);
            // 生成文件key
            String fileKey = fileStorageService.generateFileNameByType(fileType);
            MockMultipartFile file = new MockMultipartFile(fileKey, fileKey, contentType, urlConnection.getInputStream());
            return this.fileUpload(file, urlUploadDTO.getFolder(), urlUploadDTO.getUserId(), urlUploadDTO.getUserName());
        } catch (IOException e) {
            return ResponseDTO.wrap(FileResponseCodeConst.UPLOAD_ERROR);
        }
    }

    /**
     * 文件上传服务
     *
     * @param file
     * @param folderType 文件夹类型
     * @return
     */
    public ResponseDTO<FileUploadVO> fileUpload(MultipartFile file, Integer folderType, Long userId, String userName) {
        FileFolderTypeEnum folderTypeEnum = SmartBaseEnumUtil.getEnumByValue(folderType, FileFolderTypeEnum.class);
        if (null == folderTypeEnum) {
            return ResponseDTO.wrap(FileResponseCodeConst.FILE_MODULE_ERROR);
        }
        if (null == file || file.getSize() == 0) {
            return ResponseDTO.wrap(FileResponseCodeConst.FILE_EMPTY);
        }
        // 校验文件名称
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename) || originalFilename.length() > NumberLimitConst.FILE_NAME) {
            return ResponseDTO.wrap(FileResponseCodeConst.FILE_NAME_ERROR);
        }
        // 校验文件大小
        String maxSizeStr = maxFileSize.toLowerCase().replace("mb", "");
        long maxSize = Integer.parseInt(maxSizeStr) * 1024 * 1024L;
        if (file.getSize() > maxSize) {
            return ResponseDTO.wrapMsg(FileResponseCodeConst.FILE_SIZE_ERROR, String.format(FileResponseCodeConst.FILE_SIZE_ERROR.getMsg(), maxSize));
        }
        // 获取文件服务
        ResponseDTO<FileUploadVO> response = fileStorageService.fileUpload(file, folderTypeEnum.getFolder());

        if (response.isSuccess()) {
            // 上传成功 保存记录数据库
            FileUploadVO uploadVO = response.getData();

            FileEntity fileEntity = new FileEntity();
            fileEntity.setFolderType(folderTypeEnum.getValue());
            fileEntity.setFileName(originalFilename);
            fileEntity.setFileSize(file.getSize());
            fileEntity.setFileKey(uploadVO.getFileKey());
            fileEntity.setFileType(uploadVO.getFileType());
            fileEntity.setCreatorId(userId);
            fileEntity.setCreatorName(userName);
            fileDao.insert(fileEntity);
            uploadVO.setFileId(fileEntity.getId());
            //添加缓存
            String redisKey = RedisKeyConst.Base.FILE_URL + uploadVO.getFileKey();
            redisService.set(redisKey, uploadVO.getFileUrl(), fileStorageService.cacheExpireSecond());

            String fileRedisKey = RedisKeyConst.Base.FILE_VO + uploadVO.getFileKey();
            FileVO fileVO = SmartBeanUtil.copy(fileEntity, FileVO.class);
            redisService.set(fileRedisKey, fileVO, fileStorageService.cacheExpireSecond());
        }

        return response;
    }

    public List<FileVO> getFileList(List<String> fileKeyList) {
        if (CollectionUtils.isEmpty(fileKeyList)) {
            return Lists.newArrayList();
        }
        List<FileVO> fileVOList = Lists.newArrayList();
        fileKeyList.forEach(e -> {
            FileVO fileVO = this.getCacheFileVO(e);
            if (fileVO != null) {
                fileVOList.add(fileVO);
            }
        });
        return fileVOList;
    }

    private FileVO getCacheFileVO(String fileKey) {
        String redisKey = RedisKeyConst.Base.FILE_VO + fileKey;
        FileVO fileVO = redisService.getObject(redisKey, FileVO.class);
        if (fileVO == null) {
            fileVO = fileDao.getByFileKey(fileKey);
            if (fileVO != null) {
                redisService.set(redisKey, fileVO, fileStorageService.cacheExpireSecond());
            }
        }

        fileVO.setFileUrl(this.getCacheUrl(fileKey));
        return fileVO;
    }

    /**
     * 根据文件绝对路径 获取文件URL
     * 支持单个 key 逗号分隔的形式
     *
     * @param fileKey
     * @return
     */
    public ResponseDTO<String> getFileUrl(String fileKey) {
        if (StringUtils.isBlank(fileKey)) {
            return ResponseDTO.wrap(ResponseCodeConst.ERROR_PARAM);
        }
        // 处理逗号分隔的字符串
        List<String> stringList = Arrays.asList(fileKey.split(CommonConst.SEPARATOR));
        stringList = stringList.stream().map(e -> this.getCacheUrl(e)).collect(Collectors.toList());
        String result = StringUtils.join(stringList, CommonConst.SEPARATOR_CHAR);
        return ResponseDTO.succData(result);
    }


    private String getCacheUrl(String fileKey) {
        String redisKey = RedisKeyConst.Base.FILE_URL + fileKey;
        String fileUrl = redisService.get(redisKey);
        if (null != fileUrl) {
            return fileUrl;
        }
        ResponseDTO<String> responseDTO = fileStorageService.getFileUrl(fileKey);
        if (!responseDTO.isSuccess()) {
            return null;
        }
        fileUrl = responseDTO.getData();
        redisService.set(redisKey, fileUrl, fileStorageService.cacheExpireSecond());
        return fileUrl;
    }


    /**
     * 批量获取文件url
     * 支持单个 key 逗号分隔的形式
     *
     * @param queryDTO
     * @return
     */
    public ResponseDTO<List<FileUrlResultDTO>> getBatchFileUrl(FileUrlQueryDTO queryDTO) {
        // 获取文件服务
        List<String> fileKeyList = queryDTO.getFileKeyList();
        List<FileUrlResultDTO> resultDTOList = fileKeyList.stream().map(fileKey -> {
            // 处理逗号分隔的字符串
            List<String> stringList = Arrays.asList(fileKey.split(CommonConst.SEPARATOR));
            stringList = stringList.stream().map(e -> fileStorageService.getFileUrl(e).getData()).collect(Collectors.toList());
            String result = StringUtils.join(stringList, CommonConst.SEPARATOR_CHAR);
            return new FileUrlResultDTO(fileKey, result);
        }).collect(Collectors.toList());

        return ResponseDTO.succData(resultDTOList);
    }

    /**
     * 分页查询文件列表
     *
     * @param queryDTO
     * @return
     */
    public ResponseDTO<PageResultDTO<FileVO>> queryListByPage(FileQueryDTO queryDTO) {
        Page page = SmartPageUtil.convert2PageQuery(queryDTO);
        List<FileVO> fileList = fileDao.queryListByPage(page, queryDTO);
        if (CollectionUtils.isNotEmpty(fileList)) {
            fileList.forEach(e -> {
                // 根据文件服务类 获取对应文件服务 查询 url
                e.setFileUrl(fileStorageService.getFileUrl(e.getFileKey()).getData());
            });
        }
        PageResultDTO<FileVO> pageResultDTO = SmartPageUtil.convert2PageResult(page, fileList);
        return ResponseDTO.succData(pageResultDTO);
    }

    /**
     * 根据文件服务类型 和 FileKey 下载文件
     *
     * @param fileKey
     * @return
     * @throws IOException
     */
    public ResponseEntity<Object> downloadByFileKey(String fileKey, String userAgent) {
        // 根据文件服务类 获取对应文件服务 查询 url
        ResponseDTO<FileDownloadDTO> responseDTO = fileStorageService.fileDownload(fileKey);
        if (!responseDTO.isSuccess()) {
            HttpHeaders heads = new HttpHeaders();
            heads.add(HttpHeaders.CONTENT_TYPE, "text/html;charset=UTF-8");
            return new ResponseEntity<>(responseDTO.getMsg() + "：" + fileKey, heads, HttpStatus.OK);
        }
        // 设置下载头
        HttpHeaders heads = new HttpHeaders();
        heads.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream; charset=utf-8");
        // 设置对应浏览器的文件名称编码
        FileDownloadDTO fileDownloadDTO = responseDTO.getData();
        FileMetadataDTO metadata = fileDownloadDTO.getMetadata();
        String fileName = null != metadata ? metadata.getFileName() : fileKey.substring(fileKey.lastIndexOf("/"));
        fileName = fileStorageService.getDownloadFileNameByUA(fileName, userAgent);
        heads.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(fileDownloadDTO.getData(), heads, HttpStatus.OK);
        return responseEntity;
    }

    /**
     * 根据id 下载文件
     *
     * @param id
     * @return
     */
    public ResponseEntity<Object> downLoadById(Long id, String userAgent) throws IOException {
        FileEntity entity = fileDao.selectById(id);
        if (null == entity) {
            HttpHeaders heads = new HttpHeaders();
            heads.add(HttpHeaders.CONTENT_TYPE, "text/html;charset=UTF-8");
            return new ResponseEntity<>("文件不存在", heads, HttpStatus.OK);
        }

        // 根据文件服务类 获取对应文件服务 查询 url
        ResponseEntity<Object> responseEntity = this.downloadByFileKey(entity.getFileKey(), userAgent);
        return responseEntity;
    }

    /**
     * 根据文件服务和key 删除
     *
     * @param fileKey
     * @return
     */
    public ResponseDTO<String> deleteByFileKey(String fileKey) {
        if (StringUtils.isBlank(fileKey)) {
            return ResponseDTO.wrap(ResponseCodeConst.ERROR_PARAM);
        }
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileKey(fileKey);
        fileEntity = fileDao.selectOne(new QueryWrapper<>(fileEntity));
        if (null == fileEntity) {
            return ResponseDTO.wrap(FileResponseCodeConst.FILE_NOT_EXIST);
        }
        // 根据文件服务类 获取对应文件服务 删除文件
        return fileStorageService.delete(fileKey);
    }

    /**
     * 根据文件服务和key 查询文件元数据
     *
     * @param fileKey
     * @return
     */
    public ResponseDTO<FileMetadataDTO> queryFileMetadata(String fileKey) {
        if (StringUtils.isBlank(fileKey)) {
            return ResponseDTO.wrap(FileResponseCodeConst.FILE_NOT_EXIST);
        }
        // 查询数据库文件记录
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileKey(fileKey);
        fileEntity = fileDao.selectOne(new QueryWrapper<>(fileEntity));
        if (null == fileEntity) {
            return ResponseDTO.wrap(FileResponseCodeConst.FILE_NOT_EXIST);
        }

        // 返回 meta
        FileMetadataDTO metadataDTO = new FileMetadataDTO();
        metadataDTO.setFileSize(fileEntity.getFileSize());
        metadataDTO.setFileName(fileEntity.getFileName());
        metadataDTO.setFileFormat(fileEntity.getFileType());
        return ResponseDTO.succData(metadataDTO);
    }
}
