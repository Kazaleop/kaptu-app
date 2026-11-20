import { PageHeader } from '@/components/layout/PageHeader'
import { Card } from '@/components/ui'

/** Página provisória: cada rota será trocada pela tela real do Figma. */
export function EmConstrucaoPage({ title }: { title: string }) {
  return (
    <>
      <PageHeader title={title} />
      <Card>
        <p className="text-sm text-muted">Tela aguardando o design do Figma.</p>
      </Card>
    </>
  )
}
