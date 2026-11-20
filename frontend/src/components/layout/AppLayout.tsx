import { Outlet } from 'react-router-dom'
import { AlertasProvider } from '@/components/alertas/AlertasProvider'
import { Header } from './Header'
import { Sidebar } from './Sidebar'

/** Estrutura comum a todas as telas: menu lateral + cabeçalho + conteúdo. */
export function AppLayout() {
  return (
    <AlertasProvider>
      <div className="flex min-h-full">
        <Sidebar />
        <div className="flex min-w-0 flex-1 flex-col">
          <Header />
          <main className="w-full max-w-[1440px] flex-1 p-10">
            <Outlet />
          </main>
        </div>
      </div>
    </AlertasProvider>
  )
}
