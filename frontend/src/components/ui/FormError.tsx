/** Caixa vermelha com o erro devolvido pelo backend ao salvar um formulário. */
export function FormError({ error }: { error: Error | null }) {
  if (!error) return null
  return (
    <p role="alert" className="mt-4 rounded-xl bg-[#fdecea] px-3.5 py-2.5 text-sm text-[#b42318]">
      {error.message}
    </p>
  )
}
