package eventoop;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.AtributosRegras;
import br.com.sankhya.modelcore.util.EntityFacadeFactory; 

public class Addpedido implements EventoProgramavelJava{

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent event) throws Exception {
	}

	@Override
	public void afterUpdate(PersistenceEvent event) throws Exception {
		
		
		
		  DynamicVO cab = (DynamicVO) event.getVo();
		  
		  Utilitarios util = new Utilitarios();
		  List<Integer> parametros = util.buscaValorDoParametro();
		  BigDecimal topfaturamento = cab.asBigDecimal("CODTIPOPER");
		  String lancamento = cab.asString("AD_OPLANCADA");
		  Integer topfaturamentoInteger = topfaturamento.intValue();
		  BigDecimal idiproc= cab.asBigDecimal("IDIPROC");
		  BigDecimal numnota = cab.asBigDecimal("NUMNOTA");
		  
		  
		  if (JapeSession.getProperty(AtributosRegras.CONFIRMANDO) != null 
				  && parametros.contains(topfaturamentoInteger) && numnota.compareTo(BigDecimal.ZERO) > 0 && lancamento == null) { 
		  
			  SessionHandle hnd = JapeSession.open();
		         hnd.setFindersMaxRows(-1);
		        EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		        JdbcWrapper jdbc = entity.getJdbcWrapper();
		        jdbc.openSession();
		        
			  try {
	        BigDecimal nunotaorigem = cab.asBigDecimal("NUNOTA");
			NativeSql query = new NativeSql(jdbc);
			query.setNamedParameter("NUNOTA", BigDecimal.valueOf(1027430));
			query.appendSql("SELECT STATUSNOTA FROM TGFCAB WHERE NUNOTA = :NUNOTA");
			ResultSet rset = query.executeQuery();

			if (rset.next()) {
				String status = rset.getString("STATUSNOTA");

				if ("A".equals(status)) {
					BigDecimal nunpedevox=consultaNunpedevox(idiproc);
					consultapedido(nunotaorigem, BigDecimal.valueOf(1027430),nunpedevox);
				}
			}

		} catch (Exception e) {
			MGEModelException.throwMe(e);
		}

		finally {
			JapeSession.close(hnd);
			JdbcWrapper.closeSession(jdbc);
		}
	}
}

	    private BigDecimal consultaNunpedevox(BigDecimal idiproc) throws SQLException, MGEModelException {
	    	 EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	 		JdbcWrapper jdbc = entity.getJdbcWrapper();
	 		jdbc.openSession();
	 		BigDecimal nunpedevox=null;
	 		
		
		try {
		 String sql1 = "SELECT AD_NUNPEDEVOX FROM TPRIPROC WHERE IDIPROC=:IDIPROC";
        NativeSql query1 = new NativeSql(jdbc);
        query1.setNamedParameter("IDIPROC", idiproc);
        query1.appendSql(sql1);
        ResultSet rset1 = query1.executeQuery();

        if (rset1.next()) {
        	nunpedevox=rset1.getBigDecimal("AD_NUNPEDEVOX");
        }
	       

		} catch (Exception e) {
			MGEModelException.throwMe(e);
		}

		finally {
			JdbcWrapper.closeSession(jdbc);
		}
		return nunpedevox;
		
	}

		public void consultapedido(BigDecimal nunotaorigem, BigDecimal nunota, BigDecimal nunpedevox) throws Exception {
	    	 EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	 		JdbcWrapper jdbc = entity.getJdbcWrapper();
	 		jdbc.openSession();
	 		
	 		Adicionaitem add = new Adicionaitem();
		
		try {
		 String sql1 = "SELECT DISTINCT ITE.CODPROD, ITE.QTDNEG, PRO.USOPROD, PRO.AD_CLASSE_PROD\r\n"
		 		+ "		 	FROM TGFITE ITE, TGFPRO PRO \r\n"
		 		+ "		 	WHERE ITE.NUNOTA = :NUNOTA AND ITE.CODPROD = PRO.CODPROD AND PRO.USOPROD IN ('V','R')\r\n"
		 		+ "		 	AND PRO.CODGRUPOPROD IN \r\n"
		 		+ "		 	(1000300,1000400,1001000,1000600,1000700,1000800,1001100,1001300,1001400,1001500,1001600,1001700,1001900,1002000,1002100,1002100,1002200,1002300)\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050121\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050122\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050163\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050164\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050165\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050166\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050167\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050168\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050169\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2028006\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2028007\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2028009\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050050\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2030009\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2030003\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050024\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050025\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050123\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050124\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050120\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050125\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050162\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050153\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050154\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050152\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050151\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050150\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050155\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050159\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050160\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050158\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050157\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050156\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050161\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2018023\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050045\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050047\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2008061\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050043\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2012000\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2005062\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050052\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050046\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050048\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050189\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050027\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050028\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050029\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2001004\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2009048\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050044\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050051\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2005174\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050026\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2050169\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2010043\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2011072\r\n"
		 		+ "		 	AND PRO.CODPROD <> 4050501\r\n"
		 		+ "		 	AND PRO.CODPROD <> 4050041\r\n"
		 		+ "		 	AND PRO.CODPROD <> 2005110\r\n"
		 		+ "		 	AND PRO.CODPROD <> 4008001";
         NativeSql query1 = new NativeSql(jdbc);
         query1.setNamedParameter("NUNOTA", nunotaorigem);
         query1.appendSql(sql1);
         ResultSet rset1 = query1.executeQuery();

         if (rset1.next()) {
             BigDecimal codprod = rset1.getBigDecimal("CODPROD");
             BigDecimal quantidade = rset1.getBigDecimal("QTDNEG");
             
             add.consultaPreco(nunota, codprod, quantidade, nunotaorigem,nunpedevox);
    
         }
	       

		} catch (Exception e) {
			MGEModelException.throwMe(e);
		}

		finally {
			JdbcWrapper.closeSession(jdbc);
		}
	}


	    
	  
	    
//	private void consultaproduto(BigDecimal nunota,BigDecimal codprod, BigDecimal quantidade, BigDecimal nunotaorigem) throws Exception {
//		
//		Adicionaitem add = new Adicionaitem();
//		
//	    EntityFacade entity = EntityFacadeFactory.getDWFFacade();
//		JdbcWrapper jdbc = entity.getJdbcWrapper();
//		
//		try {
//		String sql = "SELECT CODPROD, QTDNEG FROM TGFITE WHERE NUNOTA = :NUNOTA";
//        NativeSql query = new NativeSql(jdbc);
//        query.setNamedParameter("NUNOTA", BigDecimal.valueOf(1027430));
//        query.appendSql(sql);
//        ResultSet rset = query.executeQuery();
//        
//        boolean encontrouLinhaValida = false;
//		
//        while (rset.next()) {
//        	BigDecimal codprodorigem = rset.getBigDecimal("CODPROD");
//        	BigDecimal quantidadeorigem = rset.getBigDecimal("QTDNEG");
//        	 if (codprodorigem.equals(codprod)) {
//                 encontrouLinhaValida = true;
//                 add.updateintens(codprodorigem, quantidadeorigem, quantidade, nunotaorigem);   
//                 break;
//             }
//         }
//
//         if (!encontrouLinhaValida) {
//             add.consultaPreco(nunota, codprod, quantidade, nunotaorigem);
//         }
//
//     } catch (Exception e) {
//         MGEModelException.throwMe(e);}
//
//		finally {
//			JdbcWrapper.closeSession(jdbc);
//		}
//	}

	
	  
		  
	  
	

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
