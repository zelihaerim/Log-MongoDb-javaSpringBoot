package com.zeliha.ayrotek;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zeliha.ayrotek.Entity.Product;
import com.zeliha.ayrotek.Repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ProductController {
	/* logging should be async
	 * printStackTrace and System.out shouln't be used
	 * Sensitive data shouldn't be included. */
	
	Logger logger =LoggerFactory.getLogger(this.getClass()); 
	
	MongoClient client= new MongoClient("localhost",27017);		
	MongoDatabase LogDB =client.getDatabase("ayrotek");
	MongoCollection<Document> logCollection= LogDB.getCollection("log");

	
	@Autowired
	private ProductRepository productRepository;
	
	@GetMapping({"/product","/"}) /* both product and / end point will go homepage */
	public String viewEmployeePage(Model model) {
		logger.debug("/: start");
		addLogMongoDB("/", "start");
		
		List<Product> listProduct = productRepository.findAll();
		
		logger.debug("/: product list received.");
		addLogMongoDB("/", "product list received.");
		
		model.addAttribute("listProduct",listProduct);
		
		logger.debug("/: end");
		addLogMongoDB("/", "end");
		
		return "productHomepage";
	}	
	
	@GetMapping("/newProduct")
	public String showNewProductForm(Model model) {	
		logger.debug("/newProduct: start");
		Product product = new Product();
		addLogMongoDB("/newProduct", "start");

		model.addAttribute("product",product);
		
		logger.debug("/newProduct: end");
		addLogMongoDB("/newProduct", "end");
		
		return "newProduct";
	}
	
	@RequestMapping(value="/saveProduct", method=RequestMethod.POST)
	public String saveProduct(@ModelAttribute("product") Product product) {
		logger.debug("/saveProduct: start");
		addLogMongoDB("/saveProduct", "start");
		try {
			productRepository.save(product);	
		}catch(IllegalArgumentException c) {
			logger.debug("/saveProduct: IllegalArgumentException");
			addLogMongoDB("/saveProduct", "IllegalArgumentException");
			return "redirect:/product";
		}
		logger.debug("/newProduct: end");
		addLogMongoDB("/newProduct", "end");
		
		return "redirect:/product";
	}
	
	@GetMapping("/editProduct/{id}")
	public String showEditProductForm(@PathVariable(name="id") Integer id,Model model) {
		try {
			logger.debug("/editProduct: start");
			addLogMongoDB("/editProduct", "start");
			
			Product product=productRepository.findById(id).get();
			model.addAttribute("product",product);
			
			logger.debug("/editProduct: end");
			addLogMongoDB("/editProduct", "end");
			
			return "editProduct";
		}catch(IllegalArgumentException c) {
			logger.debug("/editProduct: IllegalArgumentException");
			addLogMongoDB("/editProduct", "IllegalArgumentException");
			return "redirect:/product";
			
		}catch(NoSuchElementException e) {
			logger.debug("/editProduct: NoSuchElementException");
			addLogMongoDB("/editProduct", "NoSuchElementException");
			return "redirect:/product";
		}
		
	}
	
	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable(name="id") Integer id) {
		try {
			logger.debug("/deleteProduct: start");
			addLogMongoDB("/deleteProduct", "start");
			
			productRepository.deleteById(id);
			
			logger.debug("/deleteProduct: end");
			addLogMongoDB("/deleteProduct", "end");
			
		}catch(IllegalArgumentException e) {
			logger.debug("/deleteProduct: IllegalArgumentException");
			addLogMongoDB("/deleteProduct", "IllegalArgumentException");
			return "redirect:/product";
		}
		return "redirect:/product";
		
	}
	
	@GetMapping("/calculateTax/{id}")
	public String calculateTaxOfProduct(@PathVariable(name="id") Integer id,Model model) {
		
		logger.debug("/calculateTax: start");
		addLogMongoDB("/calculateTax", "start");
		Product product;
		try {
			product =productRepository.findById(id).get();
		}catch(IllegalArgumentException c) {
			logger.debug("/calculateTax: IllegalArgumentException");
			addLogMongoDB("/calculateTax", "IllegalArgumentException");
			return "redirect:/product";
			
		}catch(NoSuchElementException e) {
			logger.debug("/calculateTax: NoSuchElementException");
			addLogMongoDB("/calculateTax", "NoSuchElementException");
			return "redirect:/product";
		}
		
		Integer price =product.getPrice();
		String name=product.getName();
		model.addAttribute("name",name);
		model.addAttribute("price",price);
		
		logger.debug("/calculateTax: end");
		addLogMongoDB("/calculateTax", "end");
		
		return "calculateTax";
	}
	
	private String getCurrentTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
	private void addLogMongoDB(String path, String message) {
		String date =getCurrentTime();
		BasicDBObject data=new BasicDBObject()
				.append("path", path)
				.append("message", message)
				.append("date",date);
		logCollection.insertOne(Document.parse(data.toJson()));
	}
}
