
package com.liferay.category.portlet.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.ProcessAction;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.liferay.category.portlet.model.Asset;
import com.liferay.category.portlet.model.Category;
import com.liferay.category.portlet.model.Vocabulary;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.model.AssetCategory;
import com.liferay.portlet.asset.model.AssetVocabulary;
import com.liferay.portlet.asset.service.AssetCategoryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetVocabularyLocalServiceUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
/***
 * 
 * @author ketan.savaliya
 *
 */
/**
 * Portlet implementation class CategoryImportExportController
 */
public class CategoryImportExportController extends MVCPortlet {

	
	public Asset generateXML(ResourceRequest resourceRequest) throws PortletException, IOException {
		Asset asset = new Asset();
		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
				WebKeys.THEME_DISPLAY);
		try {
			// get all the vocabulary for group
			List<AssetVocabulary> assetVocabularyList = AssetVocabularyLocalServiceUtil.getGroupVocabularies(themeDisplay.getScopeGroupId());
			ArrayList<Vocabulary> vocabularyList = new ArrayList<Vocabulary>();
			
			for (AssetVocabulary assetVocabulary : assetVocabularyList) {
				Vocabulary vocabulary = new Vocabulary();
				
				// get child(first level) category for vocabulary
				List<AssetCategory> vocabularyCategories = AssetCategoryLocalServiceUtil.getVocabularyRootCategories(assetVocabulary.getVocabularyId(), -1, -1, null);
				
				List<Category> categoryList = new ArrayList<Category>();
				
				for (AssetCategory assetCategory : vocabularyCategories) {
					Category category = new Category();
					category.setTitle(assetCategory.getTitle(Locale.ENGLISH));
					category.setDescription(assetCategory.getDescription(Locale.ENGLISH));
					// get child categories
					int childCount = AssetCategoryLocalServiceUtil.getChildCategoriesCount(assetCategory.getCategoryId());
					category.setChildCategoryCount(childCount);
					if(childCount > 0){
						category.setCategory(getChildCategory(assetCategory.getCategoryId()));
					}
					
					categoryList.add(category);
				}
				vocabulary.setTotalChildCount(AssetCategoryLocalServiceUtil.getVocabularyCategoriesCount(assetVocabulary.getVocabularyId()));
				vocabulary.setTitle(assetVocabulary.getTitle(Locale.ENGLISH));
				vocabulary.setDescription(assetVocabulary.getDescription(Locale.ENGLISH));
				vocabulary.setCategory(categoryList);
				vocabularyList.add(vocabulary);
			}
			asset.setCompanyName(themeDisplay.getCompany().getName());
			asset.setVocabularyList(vocabularyList);

		} catch (PortalException e) {
			LOGGER.error("Error while export category structure" + e.getMessage(),e);
		} catch (SystemException e) {
			LOGGER.error("Error while export category" + e.getMessage(),e);
		} 
		return asset;
	}
	
	public List<Category> getChildCategory(long categoryId){
		List<Category> categoryList = new ArrayList<Category>();
		try {
			List<AssetCategory> childCategory = AssetCategoryLocalServiceUtil.getChildCategories(categoryId);
			for (AssetCategory assetCategory : childCategory) {
				Category category = new Category();
				category.setTitle(assetCategory.getTitle(Locale.ENGLISH));
				category.setDescription(assetCategory.getDescription(Locale.ENGLISH));
				int childCount = AssetCategoryLocalServiceUtil.getChildCategoriesCount(assetCategory.getCategoryId());
				category.setChildCategoryCount(childCount);
				if(childCount > 0){
					category.setCategory(getChildCategory(assetCategory.getCategoryId()));
				}
				categoryList.add(category);
			}
		} catch (SystemException e) {
			LOGGER.error("Error while get child category structure" + e.getMessage(),e);
		}
		return categoryList;
	}
	
	private void writeXML(Asset asset,ResourceRequest resourceRequest,ResourceResponse resourceResponse) throws IOException{
		try {
			// create JAXB context and instantiate marshaller
			JAXBContext context = JAXBContext.newInstance(Asset.class);
			
			
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			marshaller.marshal(asset,baos);
			resourceResponse.setContentType("application/xml");
			resourceResponse.addProperty(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export.xml\" ");
			resourceResponse.setContentLength(baos.size());
			OutputStream out = resourceResponse.getPortletOutputStream();
			baos.writeTo(out);
			out.flush();
			out.close();
			
		} catch (JAXBException e) {
			LOGGER.error("Error while generate file" + e.getMessage(),e);
		}
		
	}
	
	@Override
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException, PortletException {
		Asset asset = generateXML(resourceRequest);
		writeXML(asset,resourceRequest,resourceResponse);
	}
	
	@ProcessAction(name = "import")
	public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException ,PortletException {
		UploadPortletRequest uploadPortletRequest =
				PortalUtil.getUploadPortletRequest(actionRequest);
		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
				WebKeys.THEME_DISPLAY);
		File file = uploadPortletRequest.getFile("file");
		readXML(themeDisplay, file);
		

		/*String sourceFileName = uploadPortletRequest.getFileName("file");
		InputStream inputStream = null;
		String contentType = uploadPortletRequest.getContentType("file");
		long size = uploadPortletRequest.getSize("file");
		inputStream = uploadPortletRequest.getFileAsStream("file");
		*/
		
	};
	
	
	public void readXML(ThemeDisplay themeDisplay, File file){
		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setScopeGroupId(themeDisplay.getScopeGroupId());
		Asset asset = getAssetFromXml(file);
		List<Vocabulary> vocabularyList = asset.getVocabulariesList();
		for (Vocabulary vocab : vocabularyList){
				Map<Locale, String> titleMap = new HashMap<Locale, String>();
				titleMap.put(themeDisplay.getLocale(), vocab.getTitle());
				Map<Locale, String> descriptionMap = new HashMap<Locale, String>();
				descriptionMap.put(themeDisplay.getLocale(), vocab.getDescription());
				AssetVocabulary createdVocabulary = null;
				try {
					createdVocabulary = AssetVocabularyLocalServiceUtil.addVocabulary(themeDisplay.getUserId(), vocab.getTitle(), titleMap, descriptionMap, null, serviceContext);
					LOGGER.info("Import :  new vocabulary created :" + vocab.getTitle() + " : category_id : " + createdVocabulary.getVocabularyId());
				} catch (PortalException e) {
					try {
						createdVocabulary = AssetVocabularyLocalServiceUtil.getGroupVocabulary(themeDisplay.getScopeGroupId(), vocab.getTitle());
						LOGGER.info("Import : vocabulary alredy exist :" + vocab.getTitle() + " : category_id : " + createdVocabulary.getVocabularyId());
					} catch (PortalException e1) {
						LOGGER.error("Errore wihle get vocabulary with name : " +vocab.getTitle() + "  :: " +  e.getMessage(), e);
					} catch (SystemException e1) {
						LOGGER.error("Errore wihle get vocabulary with name : " +vocab.getTitle() + "  :: " +  e.getMessage(), e);
					}
				} catch (SystemException e) {
					LOGGER.error("Errore wihle create vocabulary with name : " +vocab.getTitle() + "  :: " +  e.getMessage(), e);
				}
				List<Category> categories = vocab.getCategory();
				if(categories!=null && !categories.isEmpty()){
					for(Category cat : categories){
						getChildCategory(cat,0,themeDisplay,createdVocabulary.getVocabularyId(),serviceContext);
					}
				}
		}
	}
	
	private void getChildCategory(Category cat,long parentCategoryId,ThemeDisplay themeDisplay,long vocabularyId,ServiceContext serviceContext){
		Map<Locale, String> titleMap = new HashMap<Locale, String>();
		titleMap.put(themeDisplay.getLocale(), cat.getTitle());
		Map<Locale, String> descriptionMap = new HashMap<Locale, String>();
		descriptionMap.put(themeDisplay.getLocale(), cat.getDescription());
		AssetCategory createdCategory = null;
		try {
			createdCategory = AssetCategoryLocalServiceUtil.addCategory(themeDisplay.getUserId(), parentCategoryId, titleMap, descriptionMap, vocabularyId, null, serviceContext);
			LOGGER.info("Import :  new category created :" + cat.getTitle() + " : category_id : " + createdCategory.getCategoryId());
		} catch (PortalException e) {
			try {
				
				DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(AssetCategory.class);
				dynamicQuery.add(PropertyFactoryUtil.forName("name").eq(cat.getTitle()));
				List<AssetCategory> createdCategoryList = AssetCategoryLocalServiceUtil.dynamicQuery(dynamicQuery);
				for(AssetCategory catt : createdCategoryList){
					if(catt.getParentCategoryId()==parentCategoryId && catt.getGroupId() == themeDisplay.getScopeGroupId()){
						createdCategory = catt;
						LOGGER.info("Import : category alredy exist :" + cat.getTitle() + " : category_id : " + createdCategory.getCategoryId());
						break;
					}
				}
			} catch (SystemException e1) {
				LOGGER.error("Errore wihle search category vocabulary with name : " +cat.getTitle() + "  :: " +  e1.getMessage(), e1);
			}
		} catch (SystemException e) {
			LOGGER.error("Errore wihle create category with name : " +cat.getTitle() + "  :: " +  e.getMessage(), e);
		}
		if (cat.getCategory()!= null && !cat.getCategory().isEmpty()){
			for (Category childCat : cat.getCategory()){
				getChildCategory(childCat,createdCategory.getCategoryId(),themeDisplay,vocabularyId,serviceContext);
			}
		}
	}
	
	
	private Asset getAssetFromXml(File file){
		Asset asset = null;
		try {
			if(file != null){
				JAXBContext jaxbContext = JAXBContext.newInstance(Asset.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				asset = (Asset) jaxbUnmarshaller.unmarshal(file);
				return asset;
			}
	 
		  } catch (JAXBException e) {
			  LOGGER.error("Errore wihle unmarshal for file : "  +  e.getMessage(), e);
		  }
		return asset;
	}
	private static final Log LOGGER = LogFactoryUtil.getLog(CategoryImportExportController.class); 

}
