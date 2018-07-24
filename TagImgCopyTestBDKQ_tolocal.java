import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.tqhy.Application;
import com.tqhy.filter.img.ImgCaseFilter;
import com.tqhy.filter.img.ImgCenterFilter;
import com.tqhy.filter.tag.TagImgFilter;
import com.tqhy.model.img.ImgCase;
import com.tqhy.model.img.ImgCenter;
import com.tqhy.model.tag.TagImg;
import com.tqhy.service.cm.CmAcupointService;
import com.tqhy.service.common.FastdfsService;//yl
import com.tqhy.service.img.ImgCaseService;
import com.tqhy.service.img.ImgCenterService;
import com.tqhy.service.shop.ShopItemService;
import com.tqhy.service.sys.SysMenuService;
import com.tqhy.service.tag.TagImgService;
import com.tqhy.service.tag.TagTypeService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
/**
 * 北大口腔导入数据到某一集合
 * @author tqhy_zhangting
 *
 */
public class TagImgCopyTestBDKQ_tolocal {
	@Autowired
	SysMenuService sysMenuService;
	@Autowired
	ShopItemService shopItemService;
	@Autowired
	TagTypeService tagTypeService;
	@Autowired
	CmAcupointService cmAcupointService;

	@Autowired
	TagImgService tagImgService;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	FastdfsService fastdfsService;
	@Autowired
	ImgCenterService imgCenterService;
	@Autowired
	ImgCaseService imgCaseService;

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void test() throws Exception {
		//import1();
		 //import2();
	}

	/*
	 * 需要从数据库里获取 from tag_img tg LEFT JOIN img_center imc ON
	 * tg.img_md5=imc.img_md5 left join img_case c on imc.case_id=c.id
	 * 
	 * where c.source_id='3f8b081edd0c4060805bf6a077f30679' and img_info LIKE
	 * '%脊柱侧弯%'
	 */
	public void import2(){

		 TagImgFilter filterTg = new TagImgFilter();
		 ImgCaseFilter filterC = new ImgCaseFilter();	
		 
	     filterTg.setEq_typeId("a0d61573d5b444189234bf63b2471337");
	     filterC.setEq_sourceId("3f8b081edd0c4060805bf6a077f30679"); 
	     //filterTg.setNull_tagJson(false);		 	     				
		List<TagImg> originList = tagImgService.selectAll(filterTg);		
		
		//List<Map<String,Object>> list=jdbcTemplate.queryForList("");
		int i = 0;
		for (TagImg tagImg : originList) {
			System.out.println(i);
			i++;
			String imgMd5 = tagImg.getImgMd5();
			 ImgCenterFilter filterImc = new ImgCenterFilter();
			 filterImc.setEq_imgMd5(imgMd5);
			ImgCenter imgCenter = imgCenterService.selectAll(filterImc).get(0);//service查询
			
			String caseId = imgCenter.getCaseId();
			 ImgCase imgCase = imgCaseService.getById(caseId);
			 
			 String imgInfo = imgCase.getImgInfo();
			 String imgResult = imgCase.getImgResult();
			
			tagImg.setRemark(imgInfo);
			tagImg.setDiagnosis(imgResult);
			 
			 tagImgService.save(tagImg);				
		}	
	}

	public void import1() {
		/**
		 * SELECT
		 * imc.id,imc.img_md5,imc.img_url,imc.thumb_width,imc.thumb_height
		 * ,imc.img_url_thumb,c.serial_number from img_center imc left join
		 * img_case c on imc.case_id=c.id where
		 * source_id='8310f5b97fe54a6495688263fa6ca928' and c.create_user =
		 * '2-2-1'
		 * 
		 * SELECT
		 * imc.id,imc.img_md5,imc.img_url,imc.thumb_width,imc.thumb_height
		 * ,imc.img_url_thumb from img_center imc left join img_case c on
		 * imc.case_id=c.id where source_id='8310f5b97fe54a6495688263fa6ca928'
		 * and c.create_user='正常'
		 * 
		 * SELECT
		 * imc.id,imc.img_md5,imc.img1024_url,imc.thumb_width,imc.thumb_height
		 * ,imc.img_url_thumb,c.img_info from img_center imc left join img_case
		 * c on imc.case_id=c.id where
		 * c.source_id='8310f5b97fe54a6495688263fa6ca928' and imc.img_md5
		 * in(SELECT img_md5 FROM tag_img WHERE
		 * type_id='740bc5fb849f451fad666dae1dcbb0f0' AND tag_json IS NOT NULL)
		 */

		TagImgFilter filter = new TagImgFilter();
		filter.setEq_typeId("413f225ed6a745c2b88cfb5d6053fc1d");
		filter.setNull_tagJson(false);

		List<Map<String, Object>> list = jdbcTemplate
				.queryForList("SELECT imc.img1024_url  FROM tag_img tg LEFT JOIN img_center imc ON tg.img_md5 = imc.img_md5 WHERE tg.type_id='8d86813d12bc4bd1bff0c9ddaac6cf55' AND tg.del_flag=2 AND tg.img_md5 not in (SELECT img_md5 FROM tag_img WHERE set_id='6e63fe1bbe254a759b7b4fc13922c8a9' )");

		int i = 0;

		for (Map<String, Object> tagimg : list) {
			System.out.println(i);
			i++;

			String img1024Url = tagimg.get("img1024_url").toString();
			String path = "D:/pic";
			String localPath = path + "/" + i + ".jpg";
			fastdfsService.copyFileFromDfs(img1024Url, localPath);
		}

		/*
		 * 
		 * 
		 * 
		 * System.out.println(list.size());
		 * 
		 * String setId="cc5939f3c4c34f0f8587629e9cc42fa6"; String
		 * typeId="a0d61573d5b444189234bf63b2471337";
		 * 
		 * int countadd=0; //在新的集合下排重并导入 for(Map<String,Object> tagimg:list){
		 */
		/*
		 * TagImgFilter countfilter=new TagImgFilter();
		 * countfilter.setEq_setId(setId); countfilter.setEq_typeId(typeId);
		 * countfilter.setEq_imgMd5(tagimg.get("img_md5").toString());
		 */
		// int count=0;//tagImgService.selectCountByFilter(countfilter);
		/*
		 * if(count==0){ countadd++; System.out.println(countadd); TagImg
		 * newImg=new TagImg();
		 * 
		 * newImg.setSetId(setId); newImg.setTypeId(typeId);
		 * newImg.setOrgId("81dae004980145ffbd261717b49f35b6");//bdkq2018 //
		 * newImg.setOrgId("4e252d02fc0d4855909496c977b1ba86");//bdkq //
		 * newImg.setName(tagimg.get("name").toString());
		 * newImg.setImgMd5(tagimg.get("img_md5").toString());
		 * newImg.setImgUrl(tagimg.get("img1024_url").toString());
		 * //newImg.setRemark(tagimg.get("img_info").toString());//影响描述
		 * 
		 * //newImg.setCreateUser("2018-01-24");
		 * //newImg.setTagJson(tagimg.get("tag_json").toString());//已标注
		 * newImg.setImgUrlThumb(tagimg.get("img_url_thumb").toString());
		 * newImg.
		 * setThumbw(Integer.valueOf(tagimg.get("thumb_width").toString()));
		 * newImg
		 * .setThumbh(Integer.valueOf(tagimg.get("thumb_height").toString()));
		 * newImg.setImportId(tagimg.get("id").toString());
		 * //newImg.setDiagnosis(tagimg.get("img_result").toString());//诊断意见
		 * //newImg.setSerialNumber(tagimg.get("serial_number").toString());
		 * //newImg
		 * .setOrderVal(Integer.valueOf(tagimg.get("order_val").toString()));
		 * 
		 * tagImgService.save(newImg); } }
		 * 
		 * System.out.println("共: "+list.size()+" 本次添加："+countadd);
		 */
	}

	public Map<String, String> initMap() {
		Map<String, String> map = new HashMap<String, String>();
		List<Map<String, Object>> list = jdbcTemplate
				.queryForList("SELECT imc.id,imc.img_md5,imc.img_url,imc.thumb_width,imc.thumb_height,imc.img_url_thumb,c.serial_number from img_center imc left join img_case c on imc.case_id=c.id where source_id='3f8b081edd0c4060805bf6a077f30679'and imc.case_id in(select c.id from img_case where(c.img_info like '%tb%' or c.img_info like '%结核%') or (c.img_result like '%tb%' or c.img_result like '%结核%')) and imc.id not in (select id from img_case where (c.img_info like '%陈旧性%') or (c.img_result like '%陈旧性%'or c.img_result like '%未见明显%' or c.img_result like '%复查%') or c.img_result is null or (c.img_info like '%同前片%'))");
		System.out.println(list.size());
		// 在新的集合下排重并导入
		for (Map<String, Object> tagimg : list) {
			map.put(tagimg.get("img_md5").toString(), tagimg.get("id")
					.toString());
		}
		return map;

	}
}
