import { useAuthStore } from 'stores/auth'

export default () => {
  const auth = useAuthStore()
  auth.init()
}
