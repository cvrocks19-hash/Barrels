import TaskItem from './TaskItem';
import type { Task } from '../types/task';

interface Props {
  tasks:    Task[];
  onToggle: (task: Task) => void;
  onEdit:   (task: Task) => void;
  onDelete: (id: number) => void;
}

export default function TaskList({ tasks, onToggle, onEdit, onDelete }: Props) {
  if (tasks.length === 0) {
    return (
      <div className="text-center py-20 text-gray-400">
        <p className="text-5xl mb-3">✓</p>
        <p className="text-sm">No tasks here — add one!</p>
      </div>
    );
  }
  return (
    <ul className="space-y-3">
      {tasks.map((task) => (
        <li key={task.id}>
          <TaskItem task={task} onToggle={onToggle} onEdit={onEdit} onDelete={onDelete} />
        </li>
      ))}
    </ul>
  );
}
