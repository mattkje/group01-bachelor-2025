/**
 * This file contains functions to handle HTTP requests using the Fetch API.
 */

/**
 * Represents a license that a worker can have.
 */
export interface License {
    id: number;
    name: string;
}

/**
 * Represents a zone.
 */
export interface Zone {
    id: number;
    name: string;
    capacity: number;
    isPickerZone: boolean;
    pickerTasks: PickerTask[];
    tasks: Task[];
    workers: Worker[];
}

/**
 * Represents a task an active task can be.
 */
export interface Task {
    id: number;
    name: string;
    description: string;
    minWorkers: number;
    maxWorkers: number;
    requiredLicense: License[];
    zoneId: number;
    maxTime: number;
    minTime: number;
}

/**
 * Represents a task that is currently active.
 */
export interface ActiveTask {
    id: number;
    dueDate: string;
    startTime: string;
    endTime: string;
    mcStartTime: string
    mcEndTime: string;
    eta: string;
    date: string;
    task: Task;
    workers: Worker[];
    strictStart: boolean;
}

/**
 * Represents a worker.
 */
export interface Worker {
    id: number;
    name: string;
    zone: number;
    efficiency: number;
    availability: boolean;
    dead: boolean;
    licenses: License[];
    currentTaskId: number;
}

/**
 * Represents a time table entry.
 */
export interface TimeTable {
    id: number;
    startTime: string;
    endTime: string;
    realStartTime: string;
    realEndTime: string;
    workerId: number;
}

/**
 * Represents a picker task.
 */
export interface PickerTask {
    id: number;
    distance: number;
    packAmount: number;
    linesAmount: number;
    weight: number;
    volume: number;
    avgHeight: number;
    time: number;
    startTime: string;
    endTime: string;
    mcStartTime: string
    mcEndTime: string;
    date: string;
    dueDate: string;
    zoneId: number;
    worker: Worker;
}

/**
 * Represents a world simulation object.
 */
export interface WorldSimObject {
    id: number;
    time: string;
    completedTasks: number;
    itemsPicked: number;
    realData: boolean;
    zoneId: number;
}

/**
 * Represents a notification.
 */
export interface Notification {
    id: number;
    message: string;
    zoneId: number ;
    timestamp: string | null;
}

/**
 * Represents a notification done object.
 * This is used to notify the user when a task is done.
 */
export interface NotificationDone {
    time: string;
}

/**
 * This const contains error codes and their corresponding messages.
 */
export const ErrorCodes = new Map<number, string>([
    [101, "No Tasks"],
    [102, "No Workers in Zone"],
    [103, "No Workers in Zone coming to work today"],
    [104, "Task could not complete due to workers going home"],
    [105, "No qualified workers for activetask in zone"],
]);
