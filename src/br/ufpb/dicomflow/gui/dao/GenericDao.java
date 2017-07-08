package br.ufpb.dicomflow.gui.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import br.ufpb.dicomflow.gui.dao.bean.Persistent;

public class GenericDao {


	public static void save(Persistent persistent) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			session.save(persistent);
			transaction.commit();

		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

	}

	public static void update(Persistent persistent) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			session.update(persistent);
			transaction.commit();

		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

	}

	public static void delete(Persistent persistent) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			session.delete(persistent);
			transaction.commit();

		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

	}

	public static void deleteAll(List<Persistent> persistents) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();

			Iterator<Persistent> it = persistents.iterator();
			while (it.hasNext()) {
				Persistent persistent = (Persistent) it.next();
				session.delete(persistent);
			}

			transaction.commit();

		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

	}

	@SuppressWarnings("unchecked")
	public static <T> List<Persistent> selectAll(Class<T> persistentClass){

		List<Persistent> persistents = new ArrayList<Persistent>();

		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(persistentClass);

			persistents = criteria.list();
			transaction.commit();

		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return persistents;

	}

	public static <T> Persistent select(Class<T> persistentClass, Integer id){

		Persistent persistent = null;

		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			persistent = (Persistent) session.get(persistentClass, id);
			transaction.commit();

		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return persistent;

	}

	@SuppressWarnings("unchecked")
	public static <T> List<Persistent> selectAll(Class<T> persistentClass, String property, Object value){

		List<Persistent> persistents = new ArrayList<Persistent>();

		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(persistentClass);
			criteria.add(Restrictions.eq(property, value));
			persistents = criteria.list();
			transaction.commit();

		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return persistents;

	}

	public static <T> Persistent select(Class<T> persistentClass, String property, Object value){

		Persistent persistent = null;

		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(persistentClass);
			criteria.add(Restrictions.eq(property, value));
			persistent = (Persistent) criteria.uniqueResult();
			transaction.commit();

		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return persistent;

	}


}