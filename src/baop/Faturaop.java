package baop;
import java.math.BigDecimal;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;



public class Faturaop implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao ctx) throws Exception {
		// TODO Auto-generated method stub
		
	
       
        BigDecimal numnota = BigDecimal.valueOf(((Integer) ctx.getParam("AD_NUMNOTA")).intValue());
        

        if (numnota != null) {
//            GerarNota(numnota, BigDecimal.valueOf(9167));
        }
    }

//    public void GerarNota(BigDecimal numnota, BigDecimal codtipoper) throws Exception {
//        CentralFaturamento cent = new CentralFaturamento();
//        cent.abreCabecalhoNotaOrigem(numnota, false);
//        CentralFaturamento.ConfiguracaoFaturamento cf = cent.getConfiguracaoFaturamento();
//        JapeWrapper codtipoperDao = JapeFactory.dao("TipoOperacao");
//        DynamicVO codtipoperVo = codtipoperDao.findOne("CODTIPOPER=?", new Object[]{codtipoper});
//        cf.setTipMovDest(codtipoperVo.asString("TIPMOV"));
//        cf.setUsaTopDestino(false);
//        cf.isIncluirNotaPendente();
//        cf.setSerie("1");
//        cf.setConfirmarNota(true);
//        cf.setCodTipOper(codtipoper);
//        Collection notasNunota = new ArrayList();
//        notasNunota.add(numnota);
//        JapeSession.SessionHandle hnd = null;
//        cent.getConfiguracaoFaturamento().getTipMovDest();
//        hnd = JapeSession.open();
//        Map<BigDecimal, BigDecimal> m = new HashMap();
//        m.put(numnota, BigDecimal.valueOf(0L));
//        AuthenticationInfo auth = new AuthenticationInfo("SUP", BigDecimal.ZERO, BigDecimal.ZERO, 0);
//        auth.makeCurrent();
//        FaturamentoHelper.faturar(ServiceContext.getCurrent(), hnd, cf, notasNunota, m);
//        JapeSession.close(hnd);
//        
//    }

}