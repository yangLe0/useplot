import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tqhy.Application;
import com.tqhy.filter.tag.TagImgFilter;
import com.tqhy.model.tag.TagImg;
import com.tqhy.service.common.FastdfsService;
import com.tqhy.service.img.ImgCenterService;
import com.tqhy.service.sys.SysMenuService;
import com.tqhy.service.tag.TagImgService;
import com.tqhy.service.tag.TagTypeService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yiheng
 * @create 2018/5/25
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class TestGetPositions {
    @Autowired
    ImgCenterService imgCenterService;
    @Autowired
    SysMenuService sysMenuService;
    @Autowired
    TagTypeService tagTypeService;
    @Autowired
    TagImgService tagImgService;
    @Autowired
    FastdfsService fastdfsService;

    @Test
    public void test() {
        TagImgFilter filter = new TagImgFilter();
        filter.setEq_typeId("93049f4ce47c45ae850e8e045b833795");
        filter.setNull_tagJson(false);//是否要有标注的,true是没标注
        List<TagImg> originList = tagImgService.selectAll(filter);
        List<TagImg> wrongList = new ArrayList<>();
        List<TagImg> wrongTagImgs = transTagImg(originList, wrongList);
        System.out.println("标记错误: " + wrongTagImgs.size());
    }

    /**
     * 下载jpg图片,调用{@link TestGetPositions#getPositions(String, String)}方法获取标记格子位置信息,
     * 根据位置信息调用{@link TestGetPositions#makeNewTagImg(TagImg, Integer)}方法创建具有新的typeId
     * 和setId的TagImg对象并上传.返回格子位置解析错误的TagImg对象集合.
     *
     * @param originList 待更改typeId和setId的TagImg对象集合
     * @param wrongList  格子位置解析错误的TagImg对象集合
     * @return 格子位置解析错误的TagImg对象集合
     */
    public List<TagImg> transTagImg(List<TagImg> originList, List<TagImg> wrongList) {
        List<TagImg> failedList = new ArrayList<>();
        System.out.println(originList.size());

        int i = 0;
        for (TagImg tagImg : originList) {
            i++;
            System.out.println(i);
            String imgMd5 = tagImg.getImgMd5();
            String jpgUrl = "d:/床旁并发症/" + imgMd5 + ".jpg";
            File jpgFile = new File(jpgUrl);

            if (!jpgFile.exists()) {
                try {
                    jpgFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (0 == jpgFile.length()) {
                try {
                    fastdfsService.copyFileFromDfs(tagImg.getImgUrl(), jpgUrl);
                } catch (Exception e) {
                    failedList.add(tagImg);
                    continue;
                }
            }
            String tagJson = tagImg.getTagJson();
            ArrayList<Integer> typeNumList = (ArrayList<Integer>) getPositions(jpgUrl, tagJson);
            if(typeNumList.contains(1)||typeNumList.contains(6)){
            	continue;
            }

         // for (Integer typeNum : typeNumList) {//typeNum需改
            	
            //	System.out.println(typeNum);//yl
                       		
                TagImg timg = makeNewTagImg(tagImg);
                //System.out.println(timg.getImgMd5()+"  "+timg.getTypeId());
                if (null == timg) {
                    wrongList.add(tagImg);
                    System.out.println("timg "+tagImg.getId() +" is null...");
                    break;
                }
         //   }         
            tagImgService.save(timg);
        }
        if (failedList.size() > 0) {
            transTagImg(failedList, wrongList);
        }
        return wrongList;
    }

    /**
     * 获取标记格子在jpg图片上对应的位置
     *
     * @param jpgUrl  jpg文件位置
     * @param tagJson 包含标记格子位置信息的json
     * @return 标记格子对应位置集合
     */
    public List<Integer> getPositions(String jpgUrl, String tagJson) {
        //String jpgPath = "C:\\Users\\qing\\Desktop\\rev\\777.jpg";
        //String json = "[{\"width\":310.35051546391753,\"height\":243.0103092783505,\"y\":81.97938144329896,\"x\":79.05154639175257},{\"width\":231.29896907216494,\"height\":295.7113402061855,\"y\":2356.907216494845,\"x\":131.75257731958763}]";
        List<Integer> posList = new ArrayList<Integer>();
        try {
            Type type = new TypeToken<List<ImgPositon>>() {
            }.getType();
            List<ImgPositon> positons = new Gson().fromJson(tagJson, type);
            System.out.println("positions is: " + positons);
            BufferedImage bufferedImage = ImageIO.read(new FileInputStream(new File(jpgUrl)));
            int height = bufferedImage.getHeight();
            int width = bufferedImage.getWidth();
            System.out.println("jpg height is: " + height + " width is: " + width);
            for (ImgPositon p : positons) {
                double xCenter = p.x + p.width / 2;
                double yCenter = p.y + p.height / 2;

                int xPos = (int) (xCenter * 3 / width) + 1;
                int yPos = (int) (yCenter * 3 / height) + 1;
                System.out.println("xPos is: " + xPos + " yPos is: " + yPos);
                int posNum = 0;
                switch (yPos) {
                    case 1:
                        posNum = xPos;
                        break;
                    case 2:
                        posNum = xPos + 3;
                        break;
                    case 3:
                        posNum = xPos + 6;
                        break;
                    default:
                        break;
                }
                posList.add(posNum);
            }
            System.out.println("pos is: " + posList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return posList;
    }

    /**
     * 根据原始TagImg对象创建新的TagImg对象,赋予新的setId和typeId,其他属性不变
     *
     * @param originTagImg 需要改变setId和typeId的原始TagImg对象
     * @param typeNum      标记格子对应的位置,值可能为1,3,4,5,6,7,9,如果为2或8,则表明
     *                     在{@link TestGetPositions#getPositions(String, String)}
     *                     方法中解析的位置错误.
     * @return 设置了新setId和typeId的TagImg对象
     */
    private TagImg makeNewTagImg(TagImg originTagImg) {
       
   
        TagImg newTagImg = new TagImg();
        newTagImg.setSetId("8578502f18904fd6b83ee56ff3db90e3");//a91910f8e69d4569b7829e2c6b6aa60d
        
        newTagImg.setTypeId("335516a335d9404db8df08eacb175b44");
        
        newTagImg.setImgMd5(originTagImg.getImgMd5());
        newTagImg.setRemark(originTagImg.getRemark());
        newTagImg.setImgUrlThumb(originTagImg.getImgUrlThumb());
        newTagImg.setThumbh(originTagImg.getThumbh());
        newTagImg.setThumbw(originTagImg.getThumbw());
        newTagImg.setDiagnosis(originTagImg.getDiagnosis());
        newTagImg.setSerialNumber(originTagImg.getSerialNumber());
        newTagImg.setOrderVal(originTagImg.getOrderVal());
        newTagImg.setPatientId(originTagImg.getPatientId());
        newTagImg.setTagFlag(originTagImg.getTagFlag());
        newTagImg.setOrgId(originTagImg.getOrgId());
        newTagImg.setImgUrl(originTagImg.getImgUrl());
        newTagImg.setTagJson(originTagImg.getTagJson());
        newTagImg.setTagFlag(originTagImg.getTagFlag());
        newTagImg.setDelFlag(originTagImg.getDelFlag());
        return newTagImg;
    }

    /**
     * 与jsonTag对应,表示标记格子位置信息
     */
    private class ImgPositon {
        /**
         * 格子宽
         */
        private double width;
        /**
         * 格子高
         */
        private double height;
        /**
         * 格子左上角位置距图片左边缘距离
         */
        private double x;
        /**
         * 格子左上角位置距图片顶端距离
         */
        private double y;

        @Override
        public String toString() {
            return "ImgPositon{" +
                    "width=" + width +
                    ", height=" + height +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}
