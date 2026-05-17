import { useEffect, useState } from 'react'
import { apiFetch } from '../api/client'

export default function Subjects() {
  const [subjects, setSubjects] = useState([])
  const [name, setName] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function loadSubjects() {
    setLoading(true)
    setError('')
    try {
      const data = await apiFetch('/api/subjects')
      setSubjects(data)
    } catch (err) {
      setError(err.message || 'Failed to load subjects')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadSubjects()
  }, [])

  async function handleSubmit(event) {
    event.preventDefault()
    if (!name.trim()) return
    setSubmitting(true)
    setError('')
    try {
      await apiFetch('/api/subjects', {
        method: 'POST',
        body: JSON.stringify({ name: name.trim() }),
      })
      setName('')
      await loadSubjects()
    } catch (err) {
      setError(err.message || 'Failed to add subject')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <h1>Subjects</h1>

      <form onSubmit={handleSubmit} className="inline-form">
        <input
          type="text"
          placeholder="New subject name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <button type="submit" disabled={submitting}>
          {submitting ? 'Adding...' : 'Add'}
        </button>
      </form>

      {error && <p className="form-error">{error}</p>}
      {loading && <p>Loading subjects...</p>}
      {!loading && subjects.length === 0 && <p>No subjects yet. Add one above.</p>}
      {!loading && subjects.length > 0 && (
        <ul className="data-list">
          {subjects.map((subject) => (
            <li key={subject.id}>{subject.name}</li>
          ))}
        </ul>
      )}
    </div>
  )
}
