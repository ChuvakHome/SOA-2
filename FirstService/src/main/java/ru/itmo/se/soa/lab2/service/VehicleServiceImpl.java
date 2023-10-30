package ru.itmo.se.soa.lab2.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import ru.itmo.se.soa.lab2.entity.VehicleEntity;
import ru.itmo.se.soa.lab2.model.FuelType;
import ru.itmo.se.soa.lab2.parser.ASTNode;
import ru.itmo.se.soa.lab2.parser.ASTNode.ASTNodeSubtype;
import ru.itmo.se.soa.lab2.parser.BinaryComparisonOperatorASTNode;
import ru.itmo.se.soa.lab2.parser.BinaryLogicalOperatorASTNode;
import ru.itmo.se.soa.lab2.parser.Parser;
import ru.itmo.se.soa.lab2.parser.QueryParseException;
import ru.itmo.se.soa.lab2.parser.UnaryLogicalOperatorASTNode;
import ru.itmo.se.soa.lab2.reflection.EntityUpdate;
import ru.itmo.se.soa.lab2.service.Sort.SortOrder;
import ru.itmo.se.soa.lab2.util.HibernateUtil;

@Stateless
public class VehicleServiceImpl implements VehicleService {
	@Override
	public List<VehicleEntity> findAll() {
		EntityManager entityManager = HibernateUtil.getEntityManager();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<VehicleEntity> cr = cb.createQuery(VehicleEntity.class);
		Root<VehicleEntity> root = cr.from(VehicleEntity.class);
		cr.select(root);
		
		return entityManager.createQuery(cr).getResultList();
	}
	
	public List<VehicleEntity> findAll(Sort[] sortings, String filterQuery, Pagination pagination) {
		EntityManager entityManager = HibernateUtil.getEntityManager();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<VehicleEntity> cr = cb.createQuery(VehicleEntity.class);
		Root<VehicleEntity> root = cr.from(VehicleEntity.class);
		
		cr.select(root);
		
		if (sortings != null && sortings.length > 0)
			cr.orderBy(Arrays
					.stream(sortings)
					.map(sort -> {
						Expression<?> expr = getExpressionByFieldName(root, sort.getFieldName());
						
						return sort.getSortOrder() == SortOrder.ASCENDING ?
							cb.asc(expr) :
							cb.desc(expr);
					}).toArray(Order[]::new)); 
		
		if (filterQuery != null)
			cr.where(filterQueryToExpression(filterQuery, root, cb));
		
		return pagination == null ? 
				entityManager.createQuery(cr).getResultList() :
				entityManager.createQuery(cr).setFirstResult(pagination.getPageNumber() * pagination.getPageSize()).setMaxResults(pagination.getPageSize()).getResultList(); 
	}
	
	@Override
	public Optional<VehicleEntity> findById(Long id) {
		if (id == null)
			throw new NullPointerException();
		
		return Optional.ofNullable(HibernateUtil.getEntityManager().find(VehicleEntity.class, id));
	}
	
	@Override
	public void deleteById(Long id) throws EntityNotExistException {
		EntityManager entityManager = HibernateUtil.getEntityManager();
		EntityTransaction et = entityManager.getTransaction();
		et.begin();
		
		VehicleEntity entity = entityManager.find(VehicleEntity.class, id);
		
		if (entity != null) {
			entityManager.remove(entity);
//			entityManager.flush();
			
			et.commit();
		}
		else {
			et.rollback();
			
			throw new EntityNotExistException();
		}
	}
	
	@Override
	public void deleteByEnginePower(Integer enginePower) throws EntityNotExistException {
		EntityManager entityManager = HibernateUtil.getEntityManager();
		EntityTransaction et = entityManager.getTransaction();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<Long> cr = cb.createQuery(Long.class);
		Root<VehicleEntity> root1 = cr.from(VehicleEntity.class);
		cr.select(root1.get("id"));
		cr.where(cb.equal(root1.get("enginePower"), enginePower));
		List<Long> idList = entityManager.createQuery(cr).setMaxResults(1).getResultList();
		
		if (idList.isEmpty())
			throw new EntityNotExistException();
		else {
			CriteriaDelete<VehicleEntity> cd = cb.createCriteriaDelete(VehicleEntity.class);
			Root<VehicleEntity> root = cd.from(VehicleEntity.class);
			cd.where(cb.equal(root.get("id"), idList.get(0)));
			et.begin();
			
			try {
				entityManager.createQuery(cd).executeUpdate();
				et.commit();
			} catch (Exception e) {
				et.rollback();
				
				throw e;
			}
		}
	}
	
	@Override
	public VehicleEntity create(VehicleEntity entity) throws EntityAlreadyExistsException {
		if (entity != null) {
			EntityManager entityManager = HibernateUtil.getEntityManager();
			EntityTransaction et = entityManager.getTransaction();
			et.begin();
			
			if (entity.getId() == null || entityManager.find(VehicleEntity.class, entity.getId()) == null) {
				if (entity.getCreationDate() == null)
					entity.setCreationDate(LocalDateTime.now());
				
				entityManager.persist(entity);
				
				VehicleEntity result = entityManager.merge(entity);
				et.commit();
				
				return result;
			}
			else {
				et.rollback();
				
				throw new EntityAlreadyExistsException();
			}
		}
		else
			return null;
	}
	
	@Override
	public void update(VehicleEntity entity) throws EntityNotExistException {
		if (entity != null && entity.getId() != null) {
			EntityManager entityManager = HibernateUtil.getEntityManager();
			EntityTransaction et = entityManager.getTransaction();
			et.begin();
			
			if (entityManager.find(VehicleEntity.class, entity.getId()) != null) {
				if (entity.getCreationDate() == null)
					entity.setCreationDate(LocalDateTime.now());
				
				entityManager.merge(entity);
//				entityManager.flush();
				
				et.commit();
			}
			else {
				et.rollback();
				
				throw new EntityNotExistException();
			}
		}
	}
	
	@Override
	public void update(Long id, EntityUpdate<VehicleEntity> updates) throws EntityNotExistException {
		if (id == null)
			throw new NullPointerException();
		
		EntityManager entityManager = HibernateUtil.getEntityManager();
		EntityTransaction et = entityManager.getTransaction();
		et.begin();
		
		VehicleEntity entity = entityManager.find(VehicleEntity.class, id);
		
		if (entity != null) {
			updates.applyTo(entity);
			entityManager.merge(entity);
//			entityManager.flush();
			
			et.commit();
		}
		else {
			et.rollback();
			
			throw new EntityNotExistException();
		}
	}
	
	@Override
	public double getAverageNumberOfWheels() {
		EntityManager entityManager = HibernateUtil.getEntityManager();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Double> cr = cb.createQuery(Double.class);
		Root<VehicleEntity> root = cr.from(VehicleEntity.class);
		cr.select(cb.avg(root.get("numberOfWheels")));
		
		return entityManager.createQuery(cr).getSingleResult().doubleValue();
	}
	
	public List<VehicleEntity> getVehiclesFuelTypeLessThan(FuelType type) {
		EntityManager entityManager = HibernateUtil.getEntityManager();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<VehicleEntity> cr = cb.createQuery(VehicleEntity.class);
		Root<VehicleEntity> root = cr.from(VehicleEntity.class);
		
		cr.select(root).where(cb.lt(root.get("fuelType"), type.ordinal()));
		
		return entityManager.createQuery(cr).getResultList();
	}
	
	private static<T> Path<?> getExpressionByFieldName(Root<T> root, String attributeName) {
		if (attributeName.contains(".")) {
			String[] parts = attributeName.split("\\.");
			
			Path<?> childrenRoot = root;
			
			for (String attr: parts) {
				if (attr.isBlank())
					throw new RuntimeException("Atrribute cannot be blank string");
				else
					childrenRoot = childrenRoot.get(attr);
			}
			
			return childrenRoot;
		}
		else
			return root.get(attributeName);
	}
	
//	private static void printNode0(ASTNode astNode, int tabLevel) {
//		if (astNode == null)
//			return;
//		
//		if (astNode.getNodeSubtype() != null)
//			System.out.printf("%s[%s:%s, \"%s\"]\n", "\t".repeat(tabLevel), astNode.getNodeType().name(), astNode.getNodeSubtype().name(), astNode.getNodeString());
//		else
//			System.out.printf("%s[%s, \"%s\"]\n", "\t".repeat(tabLevel), astNode.getNodeType().name(), astNode.getNodeString());
//		
//		if (astNode instanceof UnaryASTNode)
//			printNode0(((UnaryASTNode) astNode).getChild(), tabLevel + 1);
//		else if (astNode instanceof BinaryASTNode) {
//			printNode0(((BinaryASTNode) astNode).getLeftChild(), tabLevel + 1);
//			printNode0(((BinaryASTNode) astNode).getRightChild(), tabLevel + 1);
//		}
//	}
//	
//	public static void printNode(ASTNode astNode) {
//		printNode0(astNode, 0);
//	}
	
	private static<T> Expression<Boolean> filterQueryToExpression(String filterQuery, Root<T> root, CriteriaBuilder cb) {
		try {
//			printNode(Parser.parse(filterQuery));
			
			return astNodeToExpression(Parser.parse(filterQuery), root, cb);
		} catch (QueryParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static<T> Expression<Boolean> astNodeToExpression(ASTNode astNode, Root<T> root, CriteriaBuilder cb) {
		ASTNodeSubtype nodeSubtype = astNode.getNodeSubtype();
		
		if (astNode instanceof BinaryComparisonOperatorASTNode) {
			BinaryComparisonOperatorASTNode binaryComparisonOperatorASTNode = (BinaryComparisonOperatorASTNode) astNode;
			
			ASTNode leftNode = binaryComparisonOperatorASTNode.getLeftChild();
			String leftNodeString = leftNode.getNodeString();
			
			ASTNode rightNode = binaryComparisonOperatorASTNode.getRightChild();
			String rightNodeString = rightNode.getNodeString();
			
			switch (nodeSubtype) {
				case NODE_GREATER_OR_EQUAL_COMPARISON_OPERATOR:
					switch (rightNode.getNodeSubtype()) {
						case NODE_NUMBER:
							return cb.ge((Expression<? extends Number>) getExpressionByFieldName(root, leftNodeString), Double.parseDouble(rightNodeString));
						case NODE_ENUM:
							FuelType enumValue = FuelType.valueOf(rightNodeString.substring(1));
							return cb.greaterThanOrEqualTo((Expression<? extends FuelType>) getExpressionByFieldName(root, leftNodeString), enumValue);
						case NODE_DATE:
							LocalDateTime datetimeValue = LocalDate.parse(rightNodeString.substring(1)).atStartOfDay();
							return cb.greaterThanOrEqualTo((Expression<? extends LocalDateTime>) getExpressionByFieldName(root, leftNodeString), datetimeValue);
						case NODE_STRING:
							return cb.greaterThanOrEqualTo((Expression<? extends String>) getExpressionByFieldName(root, leftNodeString), rightNodeString);
						default:
							break;
					}
					break;
				case NODE_GREATER_COMPARISON_OPERATOR:
					switch (rightNode.getNodeSubtype()) {
						case NODE_NUMBER:
							return cb.gt((Expression<? extends Number>) getExpressionByFieldName(root, leftNodeString), Double.parseDouble(rightNodeString));
						case NODE_ENUM:
							FuelType enumValue = FuelType.valueOf(rightNodeString.substring(1));
							return cb.greaterThan((Expression<? extends FuelType>) getExpressionByFieldName(root, leftNodeString), enumValue);
						case NODE_DATE:
							LocalDateTime datetimeValue = LocalDate.parse(rightNodeString.substring(1)).atStartOfDay();
							return cb.greaterThan((Expression<? extends LocalDateTime>) getExpressionByFieldName(root, leftNodeString), datetimeValue);
						case NODE_STRING:
							return cb.greaterThan((Expression<? extends String>) getExpressionByFieldName(root, leftNodeString), rightNodeString);
						default:
							break;
					}
					break;
				case NODE_EQUAL_COMPARISON_OPERATOR:
					switch (rightNode.getNodeSubtype()) {
						case NODE_NUMBER:
							return cb.equal((Expression<? extends Number>) getExpressionByFieldName(root, leftNodeString), Double.parseDouble(rightNodeString));
						case NODE_ENUM:
							FuelType enumValue = FuelType.valueOf(rightNodeString.substring(1));
							return cb.equal((Expression<? extends FuelType>) getExpressionByFieldName(root, leftNodeString), enumValue);
						case NODE_DATE:
							LocalDateTime datetimeValue = LocalDate.parse(rightNodeString.substring(1)).atStartOfDay();
							return cb.equal((Expression<? extends LocalDateTime>) getExpressionByFieldName(root, leftNodeString), datetimeValue);
						case NODE_STRING:
							return cb.equal((Expression<? extends String>) getExpressionByFieldName(root, leftNodeString), rightNodeString);
						default:
							break;
					}
					break;
				case NODE_NOT_EQUAL_COMPARISON_OPERATOR:
					switch (rightNode.getNodeSubtype()) {
						case NODE_NUMBER:
							return cb.notEqual((Expression<? extends Number>) getExpressionByFieldName(root, leftNodeString), Double.parseDouble(rightNodeString));
						case NODE_ENUM:
							FuelType enumValue = FuelType.valueOf(rightNodeString.substring(1));
							return cb.notEqual((Expression<? extends FuelType>) getExpressionByFieldName(root, leftNodeString), enumValue);
						case NODE_DATE:
							LocalDateTime datetimeValue = LocalDate.parse(rightNodeString.substring(1)).atStartOfDay();
							return cb.notEqual((Expression<? extends LocalDateTime>) getExpressionByFieldName(root, leftNodeString), datetimeValue);
						case NODE_STRING:
							return cb.notEqual((Expression<? extends String>) getExpressionByFieldName(root, leftNodeString), rightNodeString);
						default:
							break;
					}
					break;
				case NODE_LESS_OR_EQUAL_COMPARISON_OPERATOR:
					switch (rightNode.getNodeSubtype()) {
						case NODE_NUMBER:
							return cb.le((Expression<? extends Number>) getExpressionByFieldName(root, leftNodeString), Double.parseDouble(rightNodeString));
						case NODE_ENUM:
							FuelType enumValue = FuelType.valueOf(rightNodeString.substring(1));
							return cb.lessThanOrEqualTo((Expression<? extends FuelType>) getExpressionByFieldName(root, leftNodeString), enumValue);
						case NODE_DATE:
							LocalDateTime datetimeValue = LocalDate.parse(rightNodeString.substring(1)).atStartOfDay();
							return cb.lessThanOrEqualTo((Expression<? extends LocalDateTime>) getExpressionByFieldName(root, leftNodeString), datetimeValue);
						case NODE_STRING:
							return cb.lessThanOrEqualTo((Expression<? extends String>) getExpressionByFieldName(root, leftNodeString), rightNodeString);
						default:
							break;
					}
					break;
				case NODE_LESS_COMPARISON_OPERATOR:
					switch (rightNode.getNodeSubtype()) {
						case NODE_NUMBER:
							return cb.lt((Expression<? extends Number>) getExpressionByFieldName(root, leftNodeString), Double.parseDouble(rightNodeString));
						case NODE_ENUM:
							FuelType enumValue = FuelType.valueOf(rightNodeString.substring(1));
							return cb.lessThan((Expression<? extends FuelType>) getExpressionByFieldName(root, leftNodeString), enumValue);
						case NODE_DATE:
							LocalDateTime datetimeValue = LocalDate.parse(rightNodeString.substring(1)).atStartOfDay();
							return cb.lessThan((Expression<? extends LocalDateTime>) getExpressionByFieldName(root, leftNodeString), datetimeValue);
						case NODE_STRING:
							return cb.lessThan((Expression<? extends String>) getExpressionByFieldName(root, leftNodeString), rightNodeString);
						default:
							break;
					}
					break;
				default:
					break;
			}
		}
		else if (astNode instanceof BinaryLogicalOperatorASTNode) {
			BinaryLogicalOperatorASTNode binaryLogicalOperatorASTNode = (BinaryLogicalOperatorASTNode) astNode;
			
			Expression<Boolean> leftExpression = astNodeToExpression(binaryLogicalOperatorASTNode.getLeftChild(), root, cb);
			Expression<Boolean> rightExpression = astNodeToExpression(binaryLogicalOperatorASTNode.getRightChild(), root, cb);
			
			switch (nodeSubtype) {
				case NODE_AND_LOGICAL_OPERATOR:
					return cb.and(leftExpression, rightExpression);
				case NODE_OR_LOGICAL_OPERATOR:
					return cb.or(leftExpression, rightExpression);
				default:
					break;
			}
		}
		else if (astNode instanceof UnaryLogicalOperatorASTNode) {
			UnaryLogicalOperatorASTNode unaryLogicalOperatorASTNode = (UnaryLogicalOperatorASTNode) astNode;
			
			if (nodeSubtype == ASTNodeSubtype.NODE_NOT_LOGICAL_OPERATOR)
				return cb.not(astNodeToExpression(unaryLogicalOperatorASTNode.getChild(), root, cb));
		}
		
		return null;
	}
}
