import { Route, Routes } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { DashboardPage } from '@/pages/Dashboard/DashboardPage'
import { EmConstrucaoPage } from '@/pages/EmConstrucao/EmConstrucaoPage'
import { AcervoPage } from '@/pages/Acervo/AcervoPage'
import { FinanceiroPage } from '@/pages/Financeiro/FinanceiroPage'
import { GestaoImobiliariaPage } from '@/pages/GestaoImobiliaria/GestaoImobiliariaPage'
import { NotFoundPage } from '@/pages/NotFound/NotFoundPage'
import { paths } from './paths'

/**
 * Todas as rotas ficam abertas (o sistema não tem login).
 * Conforme cada tela do Figma for implementada, troque o
 * <EmConstrucaoPage> pela página real.
 */
export function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path={paths.visaoGeral} element={<DashboardPage />} />
        <Route path={paths.gestaoImobiliaria} element={<GestaoImobiliariaPage />} />
        <Route path={paths.acervo} element={<AcervoPage />} />
        <Route path={paths.financeiro} element={<FinanceiroPage />} />
        <Route path={paths.pessoas} element={<EmConstrucaoPage title="Gestão de pessoas" />} />
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
